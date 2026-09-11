package my.maleva.api.module.customerstatement.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.common.service.EmailService;
import my.maleva.api.module.common.service.EmailService.EmailAttachment;
import my.maleva.api.module.common.service.ImapSentFolderService;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementSendResult;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService.RenderedStatement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Mails a Statement of Account — the port of legacy
 * {@code GenerateAndSendCustomerStatement} + {@code CustomerStamentMail}.
 *
 * <p>What is kept: the three wordings (plain statement, Reminder 1, Reminder
 * 2) with their subjects, the CC to the receivables desk, the PDF attached,
 * the banner image, the signature block, and the placeholders
 * {@code {Customer Name}}, {@code {TOTAL_OVERDUE}}, {@code {CURRENT_DATE}},
 * {@code {E_INVOICE_MONTH}}.
 *
 * <p>What is changed, and why:
 * <ul>
 *   <li><b>Templates are classpath resources.</b> Legacy fetched the default
 *       body over HTTP from {@code https://maleva.my/CustomerStatementMail.html}
 *       at send time, so mail depended on the public website being up, and the
 *       two reminders were C# string literals. All three are files under
 *       {@code resources/mail/} now — edit the wording there.</li>
 *   <li><b>The overdue figure is the statement's</b>, not a textbox. Legacy
 *       quoted whatever the browser had computed, which for an all-customers
 *       run was the last customer's balance. The amount here is the closing
 *       balance of the very statement attached, with its currency.</li>
 *   <li><b>Failure is reported.</b> Legacy caught every exception around the
 *       send and answered {@code ok: true}. This throws, and the controller
 *       turns it into an error the operator sees.</li>
 *   <li><b>Every mail is logged</b> — sent or refused — in the send log, with
 *       its own Message-ID, so the screen can show when a customer was last
 *       mailed and the mailbox reader can tie the customer's reply back to
 *       the statement (see {@code reply/StatementReplyImporter}).</li>
 *   <li><b>A copy is filed in the mailbox's Sent folder</b> over IMAP, as the
 *       Receipt screen does, so the thread is complete in Outlook too.</li>
 *   <li><b>Reminder 3–5 are the plain statement.</b> The legacy dropdown
 *       listed them; only 1 and 2 ever had a wording.</li>
 * </ul>
 *
 * <p>The message is built in {@link #prepare} and sent separately, so the bulk
 * runner can hand a batch of prepared messages to the relay over one
 * connection; {@link #send} is the single-customer path that does both.
 */
@Slf4j
@Service
public class CustomerStatementMailService {

    static final String TEMPLATE_DEFAULT = "mail/statement-default.html";
    static final String TEMPLATE_REMINDER_1 = "mail/statement-reminder1.html";
    static final String TEMPLATE_REMINDER_2 = "mail/statement-reminder2.html";
    static final String TEMPLATE_SIGNATURE = "mail/statement-signature.html";

    /** The dropdown values, exactly as the legacy screen sent them. */
    public static final String REMINDER_1 = "Reminder 1";
    public static final String REMINDER_2 = "Reminder 2";

    private static final DateTimeFormatter MAIL_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private final EmailService email;
    private final StatementMailLogRepository logs;
    private final Optional<ImapSentFolderService> sentFolder;
    private final List<String> cc;
    private final String bannerUrl;
    private final String messageIdDomain;

    public CustomerStatementMailService(
            EmailService email,
            StatementMailLogRepository logs,
            Optional<ImapSentFolderService> sentFolder,
            @Value("${mail.statement.cc:receivable@maleva.com.my,mala@maleva.com.my}") String cc,
            @Value("${mail.statement.banner-url:https://www.maleva.my/Content/images/pngimages/MalevaBanner.png}") String bannerUrl,
            @Value("${mail.from.email:account@maleva.com.my}") String fromEmail) {
        this.email = email;
        this.logs = logs;
        this.sentFolder = sentFolder;
        this.cc = splitAddresses(cc);
        this.bannerUrl = bannerUrl == null ? "" : bannerUrl.trim();
        int at = fromEmail == null ? -1 : fromEmail.indexOf('@');
        this.messageIdDomain = at > 0 ? fromEmail.substring(at + 1).trim() : "maleva.com.my";
    }

    /** A statement mail built and addressed, not yet handed to the relay. */
    public record PreparedStatementMail(MimeMessage message, String subject, List<String> to, List<String> cc,
                                        String attachmentName, String messageId) {
    }

    public List<String> cc() {
        return cc;
    }

    /**
     * Sends one customer's statement, records it and files the Sent copy.
     *
     * @throws InvalidRequestException when no usable recipient was given
     * @throws IllegalStateException   when the mail server refuses the message
     *                                 or is not configured
     */
    public StatementSendResult send(int companyId, CustomerStatement statement, RenderedStatement pdf,
                                    String emails, String reminder, String sentBy) {
        List<String> to = splitAddresses(emails);
        if (to.isEmpty()) {
            throw new InvalidRequestException("Enter at least one e-mail address to send the statement to.");
        }

        PreparedStatementMail prepared = prepare(statement, pdf, to, reminder);
        Map<MimeMessage, String> refused = email.sendPrepared(List.of(prepared.message()));
        String error = refused.get(prepared.message());
        record(companyId, statement, prepared.to(), prepared.subject(), prepared.attachmentName(), prepared.messageId(),
                reminder, error == null ? "SENT" : "FAILED", error, null, sentBy);
        if (error != null) {
            throw new IllegalStateException("The mail server refused the message: " + error);
        }
        fileSentCopy(prepared.message(), "statement " + statement.getCustomerName());

        log.info("Customer statement mailed - {} to {} recipient(s), wording '{}', {} bytes attached",
                statement.getCustomerName(), to.size(), wordingName(reminder), pdf.pdf().length);

        return new StatementSendResult(statement.getCustomerName(), prepared.subject(), to, cc, pdf.fileName(),
                statement.getOverdueAmount(), statement.getCurrency(), LocalDateTime.now());
    }

    /**
     * Build the addressed message with its PDF: subject and body by wording,
     * figures from the statement, and its own Message-ID so a reply can be
     * traced back to it.
     */
    public PreparedStatementMail prepare(CustomerStatement statement, RenderedStatement pdf,
                                         List<String> to, String reminder) {
        String subject = subjectFor(reminder, statement.getCustomerName());
        String body = bodyFor(reminder, statement);
        MimeMessage message = email.prepareHtmlMail(to, cc, subject, body,
                List.of(new EmailAttachment(pdf.fileName(), pdf.pdf(), "application/pdf")));
        String messageId = stamp(message, "statement");
        return new PreparedStatementMail(message, subject, to, cc, pdf.fileName(), messageId);
    }

    /**
     * Give the message a Message-ID of ours and return it. Spring's sender
     * keeps a Message-ID that is already set, rather than letting JavaMail
     * generate one, so the id recorded in the log is the id on the wire.
     */
    public String stamp(MimeMessage message, String prefix) {
        String messageId = "<" + prefix + "-" + UUID.randomUUID() + "@" + messageIdDomain + ">";
        try {
            message.setHeader("Message-ID", messageId);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Could not set the Message-ID: " + ex.getMessage(), ex);
        }
        return messageId;
    }

    /** File a copy of a mail that went in the mailbox's Sent folder, in the background. No-op without IMAP. */
    public void fileSentCopy(MimeMessage message, String description) {
        sentFolder.ifPresent(s -> s.appendToSentAsync(message, description));
    }

    /**
     * Write the send-log row for a statement. Never throws — see {@link StatementMailLogRepository#insert}.
     *
     * @param status SENT or FAILED
     * @param jobId  the bulk run, or null for a single send
     */
    public void record(int companyId, CustomerStatement statement, List<String> to, String subject,
                       String attachmentName, String messageId, String reminder, String status, String error,
                       String jobId, String sentBy) {
        logs.insert(new StatementMailLogRepository.Entry(
                companyId, statement.getCustomerId(), statement.getCustomerName(),
                to == null ? List.of() : to, cc, subject, wordingKey(reminder),
                statement.getStatementDate(), statement.getOverdueAmount(), statement.getCurrency(),
                attachmentName, status, error, jobId, sentBy, LocalDateTime.now(),
                StatementMailLogRepository.KIND_STATEMENT, messageId, null, null));
    }

    /** The legacy subjects, verbatim, including their en dashes. */
    String subjectFor(String reminder, String customerName) {
        if (REMINDER_1.equals(reminder)) {
            return "Friendly Payment Reminder – Outstanding Balance - " + customerName;
        }
        if (REMINDER_2.equals(reminder)) {
            return "Second Payment Reminder – Immediate Attention Required - " + customerName;
        }
        return "Statement of Account & Payment Request - " + customerName;
    }

    String bodyFor(String reminder, CustomerStatement statement) {
        String template = REMINDER_1.equals(reminder) ? TEMPLATE_REMINDER_1
                : REMINDER_2.equals(reminder) ? TEMPLATE_REMINDER_2
                : TEMPLATE_DEFAULT;

        LocalDate asOf = statement.getStatementDate() != null ? statement.getStatementDate() : LocalDate.now();
        // Legacy: the month of the overdue date, else of today.
        LocalDate overdueAsOf = statement.getOverdueAsOf() != null ? statement.getOverdueAsOf() : asOf;
        String banner = bannerUrl.isEmpty() ? ""
                : "<img src=\"" + bannerUrl + "\" style=\"width:100%; max-width:650px; margin-bottom:10px;\" alt=\"\"/>";

        return read(template)
                .replace("{SIGNATURE}", read(TEMPLATE_SIGNATURE))
                .replace("{ImageUrls}", banner)
                .replace("{Customer Name}", escape(statement.getCustomerName()))
                .replace("{CURRENT_DATE}", asOf.format(MAIL_DATE))
                .replace("{TOTAL_OVERDUE}", money(statement.getCurrency(), statement.getOverdueAmount()))
                .replace("{E_INVOICE_MONTH}", overdueAsOf.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
    }

    /** The signature block, for an answer written from the screen. */
    public String signatureHtml() {
        return read(TEMPLATE_SIGNATURE);
    }

    /** "" for the statement, else the reminder as sent — what the log stores. */
    static String wordingKey(String reminder) {
        return REMINDER_1.equals(reminder) || REMINDER_2.equals(reminder) ? reminder : "";
    }

    private static String wordingName(String reminder) {
        return REMINDER_1.equals(reminder) || REMINDER_2.equals(reminder) ? reminder : "Statement of Account";
    }

    /** "SGD 18,836.45" — the currency the statement is in, because 40% of them are not RM. */
    static String money(String currency, BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        String number = format.format(amount == null ? BigDecimal.ZERO : amount);
        return currency == null || currency.isBlank() ? number : currency.trim() + " " + number;
    }

    /** Comma or semicolon separated, trimmed, blanks dropped, duplicates dropped, order kept. */
    public static List<String> splitAddresses(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String part : raw.split("[,;]")) {
            String address = part.trim();
            if (!address.isEmpty() && address.contains("@")) {
                seen.add(address);
            }
        }
        return new ArrayList<>(seen);
    }

    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String read(String resource) {
        try (InputStream in = new ClassPathResource(resource).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Mail template " + resource + " is missing from the classpath", ex);
        }
    }
}
