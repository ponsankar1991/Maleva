package my.maleva.api.module.customerstatement.reply;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.FileUploadConfig;
import my.maleva.api.common.config.MailProperties;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.common.service.EmailService;
import my.maleva.api.module.common.service.EmailService.EmailAttachment;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.ReplyInbox;
import my.maleva.api.module.customerstatement.dto.ReplySendRequest;
import my.maleva.api.module.customerstatement.dto.ReplySendResult;
import my.maleva.api.module.customerstatement.dto.StatementConversation;
import my.maleva.api.module.customerstatement.dto.StatementReplySummary;
import my.maleva.api.module.customerstatement.dto.StatementRequest;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import my.maleva.api.module.customerstatement.mail.CustomerStatementMailService;
import my.maleva.api.module.customerstatement.mail.StatementMailLogRepository;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService.RenderedStatement;
import my.maleva.api.module.customerstatement.reply.StatementReplyRepository.Attachment;
import my.maleva.api.module.customerstatement.reply.StatementReplyRepository.Reply;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.CustomerHeader;
import my.maleva.api.module.customerstatement.service.CustomerStatementService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The conversation with a customer about their statement, and answering it
 * from the screen.
 *
 * <p>Reply and Reply All work as they do in a mail client: Reply goes to the
 * person who wrote, Reply All to everyone who was on their mail — minus our
 * own addresses, which go back on CC as on every statement mail. The answer
 * carries In-Reply-To and References pointing at the customer's mail, so it
 * lands in the same thread in their mailbox and ours, is logged like a
 * statement send, and a copy is filed in the mailbox's Sent folder.
 *
 * <p>Answers go out from the accounts address, not the operator's own; the
 * operator's name is recorded in the log as the sender.
 */
@Slf4j
@Service
public class StatementReplyService {

    private static final DateTimeFormatter QUOTE_DATE = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.ENGLISH);

    private final StatementReplyRepository replies;
    private final StatementMailLogRepository logs;
    private final CustomerStatementMailService mail;
    private final EmailService email;
    private final CustomerStatementService statements;
    private final CustomerStatementPdfService pdf;
    private final CustomerStatementQueryRepository queries;
    private final FileUploadConfig uploads;
    private final Set<String> ownAddresses;

    public StatementReplyService(StatementReplyRepository replies, StatementMailLogRepository logs,
                                 CustomerStatementMailService mail, EmailService email,
                                 CustomerStatementService statements, CustomerStatementPdfService pdf,
                                 CustomerStatementQueryRepository queries, FileUploadConfig uploads,
                                 MailProperties mailProperties) {
        this.replies = replies;
        this.logs = logs;
        this.mail = mail;
        this.email = email;
        this.statements = statements;
        this.pdf = pdf;
        this.queries = queries;
        this.uploads = uploads;
        Set<String> own = new HashSet<>();
        if (mailProperties.getFrom() != null && mailProperties.getFrom().getEmail() != null) {
            own.add(mailProperties.getFrom().getEmail().trim().toLowerCase(Locale.ROOT));
        }
        if (mailProperties.getSmtp() != null && mailProperties.getSmtp().getUsername() != null
                && mailProperties.getSmtp().getUsername().contains("@")) {
            own.add(mailProperties.getSmtp().getUsername().trim().toLowerCase(Locale.ROOT));
        }
        mail.cc().forEach(a -> own.add(a.toLowerCase(Locale.ROOT)));
        this.ownAddresses = Set.copyOf(own);
    }

    // ── reading ────────────────────────────────────────────────────────────

    public List<StatementReplySummary> latest(int companyId) {
        return replies.findLatest(companyId).stream()
                .map(s -> new StatementReplySummary(s.customerId(), s.replyCount(), s.unreadCount(), s.lastReplyId(),
                        s.lastReplyAt(), s.lastFrom(), s.lastFromName(), s.lastSubject(), snippet(s.lastSnippet())))
                .toList();
    }

    public StatementConversation conversation(int companyId, int customerId) {
        List<StatementMailLogRepository.Row> sent = logs.findByCustomer(companyId, customerId);
        List<Reply> received = replies.findByCustomer(companyId, customerId);
        Map<Long, List<Attachment>> attachments = replies.findAttachments(received.stream().map(Reply::id).toList())
                .stream().collect(Collectors.groupingBy(Attachment::replyId));

        List<StatementConversation.Entry> entries = new ArrayList<>();
        for (StatementMailLogRepository.Row r : sent) {
            entries.add(new StatementConversation.Entry("SENT", r.id(), r.sentAt(), null, r.sentTo(), r.cc(), r.subject(),
                    null, r.status(), r.error(), r.sentBy(), r.attachmentName(), false, List.of(), List.of()));
        }
        for (Reply r : received) {
            List<StatementConversation.Attachment> files = attachments.getOrDefault(r.id(), List.of()).stream()
                    .map(a -> new StatementConversation.Attachment(a.id(), a.fileName(), a.contentType(), a.sizeBytes()))
                    .toList();
            String from = r.fromName() == null || r.fromName().isBlank() ? r.fromAddress() : r.fromName() + " <" + r.fromAddress() + ">";
            entries.add(new StatementConversation.Entry("REPLY", r.id(), r.receivedAt(), from, r.to(), r.cc(), r.subject(),
                    r.bodyText(), null, null, null, null, r.readAt() == null, files,
                    recipients(ReplySendRequest.MODE_REPLY_ALL, r, ownAddresses, List.of())));
        }
        entries.sort((a, b) -> {
            int c = Objects.compare(a.at(), b.at(), java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder()));
            return c != 0 ? c : Long.compare(a.id(), b.id());
        });

        Optional<CustomerHeader> header = queries.findHeaders(companyId, List.of(customerId)).stream().findFirst();
        String name = header.map(CustomerHeader::customerName)
                .orElseGet(() -> sent.isEmpty() ? "Customer #" + customerId : sent.get(0).customerName());
        List<String> emails = header.map(h -> Stream.of(h.aEmail(), h.aEmail1(), h.oEmail(), h.oEmail1())
                .filter(e -> e != null && !e.isBlank()).map(String::trim).distinct().toList()).orElse(List.of());
        // Addresses earlier mails went to that are not on the record: the one-off ones the operator typed.
        Set<String> known = emails.stream().map(e -> e.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        List<String> previous = new ArrayList<>();
        for (StatementMailLogRepository.Row r : sent) {
            for (String a : CustomerStatementMailService.splitAddresses(r.sentTo())) {
                if (known.add(a.toLowerCase(Locale.ROOT))) {
                    previous.add(a);
                }
            }
        }
        for (Reply r : received) {
            if (r.fromAddress() != null && r.fromAddress().contains("@") && known.add(r.fromAddress().toLowerCase(Locale.ROOT))) {
                previous.add(r.fromAddress());
            }
        }
        return new StatementConversation(customerId, name, emails, previous, entries);
    }

    /** The header bell: unread replies, newest first. {@code allowed} is decided by the caller's role. */
    public ReplyInbox inbox(int companyId, int limit, boolean allowed) {
        if (!allowed) {
            return new ReplyInbox(false, 0, List.of());
        }
        List<ReplyInbox.Item> items = replies.findUnread(companyId, limit).stream()
                .map(u -> new ReplyInbox.Item(u.id(), u.customerId(),
                        u.customerName() == null || u.customerName().isBlank() ? "Customer #" + u.customerId() : u.customerName(),
                        u.fromAddress(), u.fromName(), u.subject(), snippet(u.snippet()), u.receivedAt(),
                        u.statementSubject(), u.statementSentAt()))
                .toList();
        return new ReplyInbox(true, replies.countUnread(companyId), items);
    }

    private static final java.util.regex.Pattern QUOTE_START = java.util.regex.Pattern.compile(
            "(?im)^\\s*(>|On .{5,160}wrote:|From:\\s|-----\\s*Original Message|_{5,}|Sent from my )");

    /**
     * The reader's own words: the text up to where the quoted mail begins
     * ("On … wrote:", "> …", "From:", "-----Original Message-----"), on one
     * line, at most 160 characters. A reply that is nothing but the quote
     * shows its first line.
     */
    static String snippet(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        String own = body;
        java.util.regex.Matcher m = QUOTE_START.matcher(body);
        if (m.find() && m.start() > 0) {
            own = body.substring(0, m.start());
        }
        String oneLine = own.replaceAll("\\s+", " ").trim();
        if (oneLine.isEmpty()) {
            oneLine = body.replaceAll("\\s+", " ").trim();
        }
        return oneLine.length() <= 160 ? oneLine : oneLine.substring(0, 157) + "…";
    }

    public void markRead(long replyId, int companyId, String user) {
        Reply reply = replies.findById(replyId)
                .filter(r -> r.companyId() == companyId)
                .orElseThrow(() -> new InvalidRequestException("That reply is not on file"));
        if (reply.readAt() == null) {
            replies.markRead(replyId, user);
        }
    }

    public record StoredFile(String fileName, String contentType, byte[] content) {
    }

    /** A reply's attachment, from the upload directory. Paths are kept inside it. */
    public StoredFile attachment(long replyId, long attachmentId, int companyId) throws IOException {
        replies.findById(replyId).filter(r -> r.companyId() == companyId)
                .orElseThrow(() -> new InvalidRequestException("That reply is not on file"));
        Attachment a = replies.findAttachment(replyId, attachmentId)
                .orElseThrow(() -> new InvalidRequestException("That attachment is not on file"));
        Path base = Path.of(uploads.getUploadDir()).toAbsolutePath().normalize();
        Path file = base.resolve(a.storagePath()).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            throw new InvalidRequestException("The attachment file is no longer on disk");
        }
        return new StoredFile(a.fileName(), a.contentType() == null ? "application/octet-stream" : a.contentType(), Files.readAllBytes(file));
    }

    // ── answering ──────────────────────────────────────────────────────────

    public ReplySendResult send(ReplySendRequest request, String user) {
        int companyId = request.getCompanyId();
        int customerId = request.getCustomerId();
        if (!email.isConfigured()) {
            throw new InvalidRequestException("The mail server is not configured (mail.smtp.host); nothing can be sent.");
        }
        Optional<CustomerHeader> header = queries.findHeaders(companyId, List.of(customerId)).stream().findFirst();
        String customerName = header.map(CustomerHeader::customerName).orElse("Customer #" + customerId);

        Reply original = null;
        if (request.getReplyToId() != null) {
            original = replies.findById(request.getReplyToId())
                    .filter(r -> r.companyId() == companyId && r.customerId() == customerId)
                    .orElseThrow(() -> new InvalidRequestException("The mail being answered is not on file for this customer"));
        }

        List<String> extra = CustomerStatementMailService.splitAddresses(request.getTo());
        List<String> to;
        if (original != null) {
            to = recipients(request.getMode(), original, ownAddresses, extra);
        } else {
            to = extra.isEmpty()
                    ? header.map(h -> Stream.of(h.aEmail(), h.aEmail1(), h.oEmail(), h.oEmail1())
                            .filter(e -> e != null && e.contains("@")).map(String::trim).distinct().toList()).orElse(List.of())
                    : extra;
        }
        if (to.isEmpty()) {
            throw new InvalidRequestException("There is nobody to send this to: add an address.");
        }

        String subject = request.getSubject() != null && !request.getSubject().isBlank()
                ? request.getSubject().trim()
                : original != null && original.subject() != null && !original.subject().isBlank()
                    ? StatementReplyMatcher.reSubject(original.subject())
                    : "Statement of Account - " + customerName;

        List<EmailAttachment> attachments = new ArrayList<>();
        String attachmentName = null;
        if (request.isAttachStatement()) {
            StatementRequest filters = new StatementRequest();
            filters.setCompanyId(companyId);
            filters.setCustomerId(customerId);
            StatementResult result = statements.build(filters);
            if (result.getCustomerCount() == 0) {
                throw new InvalidRequestException("This customer has nothing outstanding, so there is no statement to attach.");
            }
            RenderedStatement rendered = pdf.renderOne(result, result.getStatements().get(0));
            attachments.add(new EmailAttachment(rendered.fileName(), rendered.pdf(), "application/pdf"));
            attachmentName = rendered.fileName();
        }

        String html = bodyHtml(request.getBody(), original);
        MimeMessage message = email.prepareHtmlMail(to, mail.cc(), subject, html, attachments);
        String messageId = mail.stamp(message, "reply");
        try {
            if (original != null && original.messageId() != null && !original.messageId().isBlank()) {
                message.setHeader("In-Reply-To", original.messageId());
                String references = Stream.of(original.inReplyTo(), original.messageId())
                        .filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(" "));
                message.setHeader("References", references);
            }
        } catch (MessagingException ex) {
            throw new IllegalStateException("Could not set the threading headers: " + ex.getMessage(), ex);
        }

        Map<MimeMessage, String> refused = email.sendPrepared(List.of(message));
        String error = refused.get(message);
        CustomerStatement forLog = CustomerStatement.builder().customerId(customerId).customerName(customerName).build();
        logs.insert(new StatementMailLogRepository.Entry(
                companyId, customerId, customerName, to, mail.cc(), subject, "",
                null, null, null, attachmentName,
                error == null ? "SENT" : "FAILED", error, null, user, LocalDateTime.now(),
                StatementMailLogRepository.KIND_REPLY, messageId,
                original == null ? null : original.messageId(), original == null ? null : original.id()));
        if (error != null) {
            throw new IllegalStateException("The mail server refused the message: " + error);
        }
        mail.fileSentCopy(message, "reply to " + forLog.getCustomerName());
        if (original != null && original.readAt() == null) {
            replies.markRead(original.id(), user);
        }
        log.info("Reply to customer {} ({}) sent by {} to {} recipient(s){}", customerId, customerName, user, to.size(),
                original == null ? "" : " answering reply #" + original.id());
        return new ReplySendResult(subject, to, mail.cc(), attachmentName, LocalDateTime.now());
    }

    /**
     * Who an answer goes to. REPLY: the writer. REPLY_ALL: the writer plus
     * everyone on their To and CC, minus our own addresses (they go on CC).
     * Extras are added either way. Order kept, duplicates dropped,
     * case-insensitive.
     */
    static List<String> recipients(String mode, Reply original, Set<String> own, List<String> extra) {
        Set<String> seen = new HashSet<>();
        List<String> out = new ArrayList<>();
        List<String> candidates = new ArrayList<>();
        if (original.fromAddress() != null) {
            candidates.add(original.fromAddress());
        }
        if (ReplySendRequest.MODE_REPLY_ALL.equalsIgnoreCase(mode)) {
            candidates.addAll(CustomerStatementMailService.splitAddresses(original.to()));
            candidates.addAll(CustomerStatementMailService.splitAddresses(original.cc()));
        }
        if (extra != null) {
            candidates.addAll(extra);
        }
        for (String c : candidates) {
            String address = c.trim();
            String key = address.toLowerCase(Locale.ROOT);
            if (address.contains("@") && !own.contains(key) && seen.add(key)) {
                out.add(address);
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(out));
    }

    /** The operator's text as HTML, the signature, and the customer's mail quoted under it. */
    String bodyHtml(String text, Reply original) {
        StringBuilder html = new StringBuilder("<div style=\"font-family:Verdana,Arial,sans-serif;font-size:13px;color:#222\">");
        html.append(paragraphs(text));
        html.append("<br/>").append(mail.signatureHtml());
        if (original != null) {
            String who = original.fromName() == null || original.fromName().isBlank()
                    ? original.fromAddress()
                    : original.fromName() + " <" + original.fromAddress() + ">";
            html.append("<br/><div style=\"color:#666;font-size:12px\">On ")
                    .append(original.receivedAt() == null ? "" : original.receivedAt().format(QUOTE_DATE))
                    .append(", ").append(CustomerStatementMailService.escape(who)).append(" wrote:</div>")
                    .append("<blockquote style=\"margin:6px 0 0 0;padding-left:10px;border-left:2px solid #bbb;color:#555;font-size:12px\">")
                    .append(paragraphs(original.bodyText()))
                    .append("</blockquote>");
        }
        return html.append("</div>").toString();
    }

    private static String paragraphs(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return CustomerStatementMailService.escape(text.trim()).replace("\r\n", "\n").replace("\n", "<br/>");
    }
}
