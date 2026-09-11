package my.maleva.api.module.customerstatement.reply;

import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.FileUploadConfig;
import my.maleva.api.module.customerstatement.dto.ReplySyncResult;
import my.maleva.api.module.customerstatement.mail.StatementMailLogRepository;
import my.maleva.api.module.customerstatement.reply.StatementReplyRepository.ReplyEntry;
import my.maleva.api.module.customerstatement.reply.StatementReplyRepository.SyncState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Brings customers' replies into the system: every few minutes, read the
 * new mail in the accounts inbox, keep the ones that answer a statement we
 * sent, store them against the customer.
 *
 * <p>A mail is a statement reply when
 * <ol>
 *   <li>its In-Reply-To or References names the Message-ID of a mail in the
 *       send log (what nearly every mail client sets), or, failing that,</li>
 *   <li>its subject, with the "Re:" and "Fwd:" prefixes stripped, is exactly
 *       the subject of a mail in the send log — the fallback for the odd
 *       client that drops the headers, and for statements sent before the
 *       log carried Message-IDs.</li>
 * </ol>
 * Anything else in the inbox is left alone and never stored. Mail is only
 * read; nothing is flagged, moved or deleted, so Outlook sees the inbox as
 * before.
 *
 * <p>Attachments are written under the upload directory
 * ({@code statement-replies/<company>/}), not into the database; the row
 * keeps the relative path. The importer never runs twice at once, and a run
 * that fails leaves the position where it was so the next run retries.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "mail.statement.replies", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StatementReplyImporter {

    private final Optional<MailboxReader> reader;
    private final StatementReplyRepository replies;
    private final StatementMailLogRepository logs;
    private final FileUploadConfig uploads;
    private final Optional<StatementReplyNotifier> notifier;
    private final String folder;
    private final int initialDays;
    private final int maxPerRun;

    private final AtomicBoolean running = new AtomicBoolean();
    private volatile ReplySyncResult last;

    public StatementReplyImporter(Optional<MailboxReader> reader,
                                  StatementReplyRepository replies,
                                  StatementMailLogRepository logs,
                                  FileUploadConfig uploads,
                                  Optional<StatementReplyNotifier> notifier,
                                  @Value("${mail.statement.replies.folder:INBOX}") String folder,
                                  @Value("${mail.statement.replies.initial-days:7}") int initialDays,
                                  @Value("${mail.statement.replies.max-per-run:200}") int maxPerRun) {
        this.reader = reader;
        this.replies = replies;
        this.logs = logs;
        this.uploads = uploads;
        this.notifier = notifier;
        this.folder = folder == null || folder.isBlank() ? "INBOX" : folder.trim();
        this.initialDays = Math.max(1, initialDays);
        this.maxPerRun = Math.max(1, maxPerRun);
        if (reader.isEmpty()) {
            log.warn("Statement reply reader idle: no IMAP host configured (mail.imap.host)");
        }
    }

    @Scheduled(fixedDelayString = "${mail.statement.replies.poll-ms:180000}",
            initialDelayString = "${mail.statement.replies.initial-delay-ms:45000}")
    public void scheduled() {
        sync();
    }

    /** The last run's outcome, for the screen. */
    public ReplySyncResult status() {
        ReplySyncResult l = last;
        return new ReplySyncResult(mailbox(), folder, l == null ? 0 : l.scanned(), l == null ? 0 : l.imported(),
                l == null ? null : l.ranAt(), l == null ? null : l.error(), running.get());
    }

    /** Read the mailbox now. Returns at once with "already running" if a run is in progress. */
    public ReplySyncResult sync() {
        if (reader.isEmpty()) {
            return remember(new ReplySyncResult(mailbox(), folder, 0, 0, LocalDateTime.now(),
                    "IMAP is not configured (mail.imap.host)", false));
        }
        if (!running.compareAndSet(false, true)) {
            return new ReplySyncResult(mailbox(), folder, 0, 0, LocalDateTime.now(), "A mailbox scan is already running", true);
        }
        try {
            return remember(run(reader.get()));
        } finally {
            running.set(false);
        }
    }

    private ReplySyncResult run(MailboxReader mailbox) {
        String name = mailbox.mailboxName();
        long started = System.nanoTime();
        AtomicInteger imported = new AtomicInteger();
        Map<Integer, Integer> perCompany = new ConcurrentHashMap<>();
        try {
            SyncState state = replies.syncState(name, folder).orElse(null);
            MailboxReader.Scan scan = mailbox.scan(folder,
                    state == null ? null : state.uidValidity(),
                    state == null ? 0 : state.lastUid(),
                    initialDays, maxPerRun,
                    (head, body) -> {
                        Optional<StatementMailLogRepository.Row> match = match(head);
                        if (match.isEmpty() || replies.exists(name, folder, head.uid())) {
                            return;
                        }
                        store(name, head, body.get(), match.get());
                        imported.incrementAndGet();
                        perCompany.merge(match.get().companyId(), 1, Integer::sum);
                    });
            replies.saveSyncState(new SyncState(name, folder, scan.uidValidity(), scan.maxUid(), LocalDateTime.now(), null));
            // the position is saved first: a browser told too early would only refetch and see nothing yet
            notifier.ifPresent(n -> perCompany.forEach(n::newReplies));
            log.info("Mailbox {}/{}: {} mail(s) looked at, {} statement repl{} imported in {} ms",
                    name, folder, scan.scanned(), imported.get(), imported.get() == 1 ? "y" : "ies",
                    (System.nanoTime() - started) / 1_000_000);
            return new ReplySyncResult(name, folder, scan.scanned(), imported.get(), LocalDateTime.now(), null, false);
        } catch (Exception ex) {
            String why = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            log.warn("Mailbox {}/{} scan failed: {}", name, folder, why);
            replies.saveSyncError(name, folder, why);
            return new ReplySyncResult(name, folder, 0, imported.get(), LocalDateTime.now(), why, false);
        }
    }

    /**
     * Headers first, subject second, sender third — see the class comment.
     *
     * <p>The third rule is for the customer who writes from a one-off or
     * temporary address that is not on their record: if a reply-style mail
     * (it had a "Re:"/"Fwd:" to strip) comes from an address we have sent a
     * statement to, it belongs to that customer. A mail with no reply prefix
     * is never taken by sender alone — that would sweep up unrelated mail.
     */
    Optional<StatementMailLogRepository.Row> match(InboundMail head) {
        for (String id : StatementReplyMatcher.candidateIds(head)) {
            Optional<StatementMailLogRepository.Row> row = logs.findByMessageId(id);
            if (row.isPresent()) {
                return row;
            }
        }
        String subject = StatementReplyMatcher.normaliseSubject(head.subject());
        if (subject.isEmpty() || subject.equalsIgnoreCase(head.subject() == null ? "" : head.subject().trim())) {
            // no prefix was stripped: not a reply to anything, whatever the subject says
            return Optional.empty();
        }
        Optional<StatementMailLogRepository.Row> bySubject = logs.findLatestBySubject(subject);
        if (bySubject.isPresent()) {
            return bySubject;
        }
        return logs.findLatestBySentTo(head.fromAddress());
    }

    private void store(String mailbox, InboundMail head, InboundMail.Body body, StatementMailLogRepository.Row statement) {
        String text = body.text() != null ? body.text() : htmlToText(body.html());
        long id = replies.insert(new ReplyEntry(
                statement.companyId(), statement.customerId(), statement.id(),
                mailbox, folder, head.uid(), head.messageId(), head.inReplyTo(),
                head.fromAddress(), head.fromName(), head.to(), head.cc(), head.subject(),
                text, body.html(), !body.attachments().isEmpty(), head.receivedAt()));
        for (InboundMail.Attachment attachment : body.attachments()) {
            try {
                String relative = save(statement.companyId(), head.uid(), attachment);
                replies.insertAttachment(id, attachment.fileName(), attachment.contentType(), attachment.content().length, relative);
            } catch (IOException ex) {
                log.warn("Reply {}: attachment {} could not be saved: {}", id, attachment.fileName(), ex.getMessage());
            }
        }
        log.info("Statement reply from {} for customer {} ({}) stored as #{}{}",
                head.fromAddress(), statement.customerId(), statement.customerName(), id,
                body.attachments().isEmpty() ? "" : " with " + body.attachments().size() + " attachment(s)");
    }

    /** {@code <upload-dir>/statement-replies/<company>/<uid>-<safe name>}; the relative part is what the row keeps. */
    private String save(int companyId, long uid, InboundMail.Attachment attachment) throws IOException {
        String safe = (attachment.fileName() == null ? "attachment" : attachment.fileName())
                .replaceAll("[^A-Za-z0-9._-]+", "_").replaceAll("^_+", "");
        if (safe.isEmpty()) {
            safe = "attachment";
        }
        Path relative = Path.of("statement-replies", String.valueOf(companyId), uid + "-" + safe);
        Path target = Path.of(uploads.getUploadDir()).resolve(relative);
        Files.createDirectories(target.getParent());
        Files.write(target, attachment.content());
        return relative.toString().replace('\\', '/');
    }

    /** A readable fallback for HTML-only replies: tags dropped, common entities unescaped. */
    static String htmlToText(String html) {
        if (html == null) {
            return null;
        }
        String s = html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</(p|div|tr|li|h[1-6])>", "\n")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#39;", "'");
        return s.replaceAll("[ \\t\\x0B\\f\\r]+", " ").replaceAll("\\n\\s*\\n+", "\n\n").trim();
    }

    private String mailbox() {
        return reader.map(MailboxReader::mailboxName).orElse("");
    }

    private ReplySyncResult remember(ReplySyncResult result) {
        last = result;
        return result;
    }

    List<String> foldersRead() {
        return List.of(folder);
    }
}
