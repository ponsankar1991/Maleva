package my.maleva.api.module.customerstatement.mail;

import jakarta.annotation.PreDestroy;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.common.service.EmailService;
import my.maleva.api.module.common.service.EmailService.EmailAttachment;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementAttach;
import my.maleva.api.module.customerstatement.dto.StatementMailJobRequest;
import my.maleva.api.module.customerstatement.dto.StatementMailJobView;
import my.maleva.api.module.customerstatement.dto.StatementRequest;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import my.maleva.api.module.customerstatement.mail.CustomerStatementMailService.PreparedStatementMail;
import my.maleva.api.module.customerstatement.mail.StatementMailJob.Item;
import my.maleva.api.module.customerstatement.mail.StatementMailJob.ItemStatus;
import my.maleva.api.module.customerstatement.print.CustomerStatementExcelService;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService.RenderedStatement;
import my.maleva.api.module.customerstatement.service.CustomerStatementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bulk statement runs — "send these N customers their statement", in the
 * background, over as few SMTP connections as possible.
 *
 * <p>Why a job and not a loop in the request: a 500-customer round at half
 * a second a mail is minutes, longer than a browser waits, and the operator
 * wants to watch it go and stop it. So {@link #start} builds every statement
 * in ONE query pass (the same one the View runs for "all customers"), lines
 * the customers up, answers at once with the job, and a single worker thread
 * does the sending while the screen polls {@link #get}.
 *
 * <p>Why one worker: there is one accounts mailbox, and hosting relays
 * throttle by connection. One thread sending {@code batch-size} messages per
 * connection is what the relay is happiest with; the pause setting is there
 * for a relay that still objects.
 *
 * <p>Where the speed comes from, in order: the statements are built once
 * for the whole run, not per customer; the report template is compiled once
 * per process; each connection carries a batch of messages instead of one;
 * and nothing waits on the browser. A refused mail marks that customer
 * failed and the run goes on; a refused login stops the run, because every
 * later batch would fail the same way.
 *
 * <p>One run per company at a time — a second Send while one is going would
 * mail the same customers twice.
 */
@Slf4j
@Service
public class StatementMailJobService {

    /** How long a finished run stays readable; the send log is the durable record. */
    static final Duration RETENTION = Duration.ofHours(6);

    private final CustomerStatementService statements;
    private final CustomerStatementPdfService pdf;
    private final CustomerStatementExcelService excel;
    private final CustomerStatementMailService mail;
    private final EmailService email;
    private final int batchSize;
    private final long pauseBetweenBatchesMs;
    private final Executor worker;
    private final Map<String, StatementMailJob> jobs = new ConcurrentHashMap<>();

    @Autowired
    public StatementMailJobService(CustomerStatementService statements, CustomerStatementPdfService pdf,
                                   CustomerStatementExcelService excel,
                                   CustomerStatementMailService mail, EmailService email,
                                   @Value("${mail.statement.batch-size:10}") int batchSize,
                                   @Value("${mail.statement.pause-between-batches-ms:0}") long pauseBetweenBatchesMs) {
        this(statements, pdf, excel, mail, email, batchSize, pauseBetweenBatchesMs,
                Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "statement-mail");
                    t.setDaemon(true);
                    return t;
                }));
    }

    /** For tests: run the worker on the caller's thread. */
    StatementMailJobService(CustomerStatementService statements, CustomerStatementPdfService pdf,
                            CustomerStatementMailService mail, EmailService email,
                            int batchSize, long pauseBetweenBatchesMs, Executor worker) {
        this(statements, pdf, new CustomerStatementExcelService(), mail, email, batchSize, pauseBetweenBatchesMs, worker);
    }

    StatementMailJobService(CustomerStatementService statements, CustomerStatementPdfService pdf,
                            CustomerStatementExcelService excel,
                            CustomerStatementMailService mail, EmailService email,
                            int batchSize, long pauseBetweenBatchesMs, Executor worker) {
        this.statements = statements;
        this.pdf = pdf;
        this.excel = excel;
        this.mail = mail;
        this.email = email;
        this.batchSize = Math.max(1, batchSize);
        this.pauseBetweenBatchesMs = Math.max(0, pauseBetweenBatchesMs);
        this.worker = worker;
    }

    /**
     * Build the statements, line up the customers and start sending.
     *
     * @throws InvalidRequestException when a run is already going for this
     *                                 company, or no customer was named
     */
    public StatementMailJobView start(StatementMailJobRequest request, String requestedBy) {
        purgeExpired();
        int companyId = request.getCompanyId();
        active(companyId).ifPresent(view -> {
            throw new InvalidRequestException("A statement run is already in progress for this company ("
                    + (view.total() - view.pending()) + " of " + view.total()
                    + " done). Wait for it to finish, or cancel it.");
        });
        if (!email.isConfigured()) {
            throw new InvalidRequestException("The mail server is not configured (mail.smtp.host); nothing can be sent.");
        }

        StatementRequest filters = new StatementRequest();
        filters.setCompanyId(companyId);
        filters.setCustomerId(null);           // every customer, once; the list below picks
        filters.setIncludeCreditNotes(request.isIncludeCreditNotes());
        filters.setUseDateRange(request.isUseDateRange());
        filters.setFromDate(request.getFromDate());
        filters.setToDate(request.getToDate());
        StatementResult result = statements.build(filters);
        Map<Integer, CustomerStatement> byId = result.getStatements().stream()
                .collect(Collectors.toMap(CustomerStatement::getCustomerId, Function.identity(), (a, b) -> a));

        List<Item> items = new ArrayList<>(request.getCustomers().size());
        Set<Integer> seen = new HashSet<>();
        for (StatementMailJobRequest.Recipient recipient : request.getCustomers()) {
            int id = recipient.getCustomerId();
            if (!seen.add(id)) {
                continue;                       // named twice: mail once
            }
            CustomerStatement statement = byId.get(id);
            if (statement == null) {
                items.add(new Item(id, "Customer #" + id, List.of(), null,
                        ItemStatus.SKIPPED, "No outstanding invoices under these filters"));
                continue;
            }
            List<String> to = recipient.getEmails() == null
                    ? statement.getEmails()
                    : CustomerStatementMailService.splitAddresses(recipient.getEmails());
            if (to == null || to.isEmpty()) {
                items.add(new Item(id, statement.getCustomerName(), List.of(), statement,
                        ItemStatus.SKIPPED, "No e-mail address"));
                continue;
            }
            items.add(new Item(id, statement.getCustomerName(), to, statement, ItemStatus.PENDING, null));
        }

        StatementMailJob job = new StatementMailJob(UUID.randomUUID().toString(), companyId,
                request.getReminder(), StatementAttach.of(request.getAttach()), requestedBy, result, items);
        jobs.put(job.id(), job);
        StatementMailJobView queued = job.view();
        log.info("Statement run {} queued by {} - company {}, {} customer(s): {} to send, {} skipped, wording '{}', attaching {}",
                job.id(), requestedBy, companyId, queued.total(), queued.pending(), queued.skipped(), job.reminder(), job.attach());
        worker.execute(() -> run(job));
        // Snapshot after hand-off: the worker may already be on it, and the
        // screen's first poll should not show a run as queued that is running.
        return job.view();
    }

    public Optional<StatementMailJobView> get(String id) {
        purgeExpired();
        return Optional.ofNullable(jobs.get(id)).map(StatementMailJob::view);
    }

    /** The run in progress for the company, if any — so a reloaded screen can pick it up again. */
    public Optional<StatementMailJobView> active(int companyId) {
        return jobs.values().stream()
                .filter(job -> job.companyId() == companyId && job.isActive())
                .findFirst()
                .map(StatementMailJob::view);
    }

    /** Stop after the batch in flight; customers not reached are marked cancelled, none is half-sent. */
    public Optional<StatementMailJobView> cancel(String id) {
        StatementMailJob job = jobs.get(id);
        if (job == null) {
            return Optional.empty();
        }
        if (job.isActive()) {
            job.requestCancel();
            log.info("Statement run {} cancel requested", id);
        }
        return Optional.of(job.view());
    }

    void run(StatementMailJob job) {
        job.markRunning();
        long started = System.nanoTime();
        try {
            List<Item> pending = job.items().stream().filter(i -> i.status == ItemStatus.PENDING).toList();
            for (int from = 0; from < pending.size(); from += batchSize) {
                if (job.isCancelRequested()) {
                    break;
                }
                sendBatch(job, pending.subList(from, Math.min(from + batchSize, pending.size())));
                if (pauseBetweenBatchesMs > 0 && from + batchSize < pending.size()) {
                    Thread.sleep(pauseBetweenBatchesMs);
                }
            }
        } catch (IllegalStateException ex) {
            // The relay refused the login or is not configured: every later batch would fail the same way.
            log.error("Statement run {} stopped: {}", job.id(), ex.getMessage());
            job.fail(ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            job.fail("The run was interrupted");
        } catch (RuntimeException ex) {
            log.error("Statement run {} crashed", job.id(), ex);
            job.fail("Unexpected error: " + root(ex));
        } finally {
            job.finish();
            job.release();
            StatementMailJobView view = job.view();
            log.info("Statement run {} {} in {} ms - {} sent, {} failed, {} skipped, {} cancelled",
                    job.id(), view.status(), (System.nanoTime() - started) / 1_000_000,
                    view.sent(), view.failed(), view.skipped(), view.cancelled());
        }
    }

    /**
     * Render and address every message of the batch, hand them to the relay
     * over one connection, then record each outcome. A customer whose PDF or
     * address cannot be prepared fails alone; the batch goes on.
     */
    private void sendBatch(StatementMailJob job, List<Item> batch) {
        Map<MimeMessage, Item> byMessage = new LinkedHashMap<>();       // MimeMessage has identity equality
        Map<Item, PreparedStatementMail> prepared = new LinkedHashMap<>();
        for (Item item : batch) {
            item.status = ItemStatus.SENDING;
            try {
                PreparedStatementMail message = prepare(job, item);
                byMessage.put(message.message(), item);
                prepared.put(item, message);
            } catch (RuntimeException ex) {
                String why = "Could not prepare the mail: " + root(ex);
                item.failed(why);
                mail.record(job.companyId(), item.statement, item.emails, null, null, null, job.reminder(),
                        "FAILED", why, job.id(), job.requestedBy());
            }
        }
        if (byMessage.isEmpty()) {
            return;
        }

        Map<MimeMessage, String> refused = email.sendPrepared(new ArrayList<>(byMessage.keySet()));

        for (Map.Entry<MimeMessage, Item> entry : byMessage.entrySet()) {
            Item item = entry.getValue();
            PreparedStatementMail message = prepared.get(item);
            String error = refused.get(entry.getKey());
            if (error == null) {
                item.sent(message.subject(), message.attachmentName());
                mail.fileSentCopy(entry.getKey(), "statement " + item.customerName);
            } else {
                item.failed(error);
            }
            mail.record(job.companyId(), item.statement, message.to(), message.subject(), message.attachmentName(),
                    message.messageId(), job.reminder(), error == null ? "SENT" : "FAILED", error, job.id(), job.requestedBy());
        }
    }

    /**
     * One customer's message with the run's file(s): the PDF alone takes the
     * original path; Excel, or both, renders the workbook for that customer too.
     */
    private PreparedStatementMail prepare(StatementMailJob job, Item item) {
        StatementAttach attach = job.attach();
        if (attach == StatementAttach.PDF) {
            RenderedStatement rendered = pdf.renderOne(job.result(), item.statement);
            return mail.prepare(item.statement, rendered, item.emails, job.reminder());
        }
        List<EmailAttachment> files = new ArrayList<>(2);
        if (attach.pdf()) {
            files.add(CustomerStatementMailService.pdfAttachment(pdf.renderOne(job.result(), item.statement)));
        }
        CustomerStatementExcelService.RenderedWorkbook workbook = excel.renderOne(job.result(), item.statement);
        files.add(new EmailAttachment(workbook.fileName(), workbook.content(), CustomerStatementExcelService.CONTENT_TYPE));
        return mail.prepare(item.statement, files, item.emails, job.reminder(), CustomerStatementMailService.MailOverrides.NONE);
    }

    private void purgeExpired() {
        LocalDateTime limit = LocalDateTime.now().minus(RETENTION);
        jobs.values().removeIf(job -> !job.isActive() && job.finishedAt() != null && job.finishedAt().isBefore(limit));
    }

    private static String root(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage().trim();
    }

    @PreDestroy
    void shutdown() {
        if (worker instanceof ExecutorService service) {
            service.shutdownNow();
        }
    }
}
