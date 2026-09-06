package my.maleva.api.module.paymentrecept.mail;

import jakarta.mail.internet.MimeMessage;
import my.maleva.api.common.config.MailProperties;
import my.maleva.api.module.common.service.EmailService;
import my.maleva.api.module.common.service.EmailService.EmailAttachment;
import my.maleva.api.module.common.service.ImapSentFolderService;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.filehandling.dto.AttachmentDto;
import my.maleva.api.module.filehandling.model.AttachmentScope;
import my.maleva.api.module.filehandling.service.AttachmentStorageService;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.paymentrecept.dto.ReceiptMailInfoDto;
import my.maleva.api.module.paymentrecept.dto.ReceiptMailRequest;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import my.maleva.api.module.paymentrecept.print.ReceiptPdfService;
import my.maleva.api.module.paymentrecept.repository.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The SEND MAIL button of the Receipt screen — the port of legacy
 * {@code ReceiptController.ReceiptMailInfo} + {@code SendReceiptMail} and of
 * {@code commonfunctions.GenerateAndSendReceiptMail} / {@code ReceiptMail}.
 *
 * <p>Legacy exported the Crystal voucher to a file under {@code /Pdf}, built
 * the body from {@code ReceiptMail.html}, sent through a mailbox whose
 * password was compiled into the site, and appended a copy to the Sent
 * folder over IMAP. Here the voucher is the Jasper PDF rendered in memory,
 * the body is the same template read from the classpath, the SMTP and IMAP
 * accounts come from configuration, and the operator can also send the
 * files stored against the receipt. Every failure path answers with the
 * reason instead of a blank.
 */
@Service
public class ReceiptMailService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptMailService.class);
    private static final String TEMPLATE = "mail/receipt-mail.html";
    /** Storage folder the legacy screen used for receipt attachments. */
    static final String ATTACHMENT_FOLDER = "Receipt";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private final ReceiptRepository receipts;
    private final CustomerRepository customers;
    private final SymbolMasterRepository symbols;
    private final ReceiptPdfService pdfService;
    private final AttachmentStorageService attachments;
    private final EmailService emailService;
    private final Optional<ImapSentFolderService> sentFolder;
    private final MailProperties mailProperties;

    public ReceiptMailService(ReceiptRepository receipts, CustomerRepository customers,
                              SymbolMasterRepository symbols, ReceiptPdfService pdfService,
                              AttachmentStorageService attachments, EmailService emailService,
                              Optional<ImapSentFolderService> sentFolder, MailProperties mailProperties) {
        this.receipts = receipts;
        this.customers = customers;
        this.symbols = symbols;
        this.pdfService = pdfService;
        this.attachments = attachments;
        this.emailService = emailService;
        this.sentFolder = sentFolder;
        this.mailProperties = mailProperties;
    }

    /** What the mail window is prefilled with; empty when the receipt is not this company's. */
    public Optional<ReceiptMailInfoDto> info(Integer receiptId, Integer companyId) {
        Receipt receipt = find(receiptId, companyId);
        if (receipt == null) {
            return Optional.empty();
        }
        Customer customer = receipt.getCustomerRefId() == null ? null
                : customers.findById(receipt.getCustomerRefId()).orElse(null);
        List<String> customerEmails = customer == null ? List.of()
                : ReceiptMailRecipients.split(Stream.of(customer.getAEmail(), customer.getAEmail1(),
                        customer.getOEmail(), customer.getOEmail1()).filter(Objects::nonNull).toList());
        List<String> files = new ArrayList<>();
        try {
            for (AttachmentDto stored : attachments.list(AttachmentScope.of(companyId, ATTACHMENT_FOLDER, receiptId, null))) {
                files.add(stored.getFileName());
            }
        } catch (RuntimeException ex) {
            log.warn("Receipt {} attachment folder could not be listed: {}", receiptId, ex.getMessage());
        }
        return Optional.of(ReceiptMailInfoDto.builder()
                .receiptId(receipt.getId())
                .receiptNo(orEmpty(receipt.getCNumberDisplay()))
                .receiptDate(receipt.getReceiptDate() == null ? "" : receipt.getReceiptDate().toLocalDate().format(DATE))
                .customerName(customer == null ? "" : orEmpty(customer.getCustomerName()))
                .refNumber(orEmpty(receipt.getRefNumber()))
                .amount(money(receipt.getAmount()))
                .currencySymbol(currencySymbol(customer))
                .customerEmails(customerEmails)
                .defaultCc(ReceiptMailRecipients.split(mailProperties.getReceiptCc()))
                .subject(defaultSubject())
                .attachmentFiles(files)
                .mailConfigured(emailService.isConfigured())
                .build());
    }

    /** Renders the voucher and mails it; the outcome says exactly what happened. */
    public MailOutcome send(Integer receiptId, Integer companyId, ReceiptMailRequest request) {
        if (request == null) {
            return MailOutcome.failure("Nothing to send");
        }
        List<String> to = ReceiptMailRecipients.split(request.getTo());
        List<String> cc = ReceiptMailRecipients.split(request.getCc());
        if (to.isEmpty()) {
            return MailOutcome.failure("Please enter at least one valid Email Id in the To box");
        }
        List<String> bad = new ArrayList<>(ReceiptMailRecipients.invalid(to));
        bad.addAll(ReceiptMailRecipients.invalid(cc));
        if (!bad.isEmpty()) {
            return MailOutcome.failure("Not a valid email: " + String.join(", ", bad));
        }
        if (!emailService.isConfigured()) {
            return MailOutcome.failure("The mail server is not configured on this server (mail.smtp.host)");
        }

        Receipt receipt = find(receiptId, companyId);
        if (receipt == null) {
            return MailOutcome.failure("Receipt " + receiptId + " was not found for this company");
        }
        Customer customer = receipt.getCustomerRefId() == null ? null
                : customers.findById(receipt.getCustomerRefId()).orElse(null);
        String receiptNo = orEmpty(receipt.getCNumberDisplay()).isBlank() ? String.valueOf(receiptId) : receipt.getCNumberDisplay();

        Optional<ReceiptPdfService.RenderedReceipt> rendered;
        try {
            rendered = pdfService.render(receiptId, companyId);
        } catch (RuntimeException ex) {
            log.error("Receipt {} PDF could not be built for the mail", receiptNo, ex);
            return MailOutcome.failure("Receipt PDF could not be prepared: " + rootMessage(ex));
        }
        if (rendered.isEmpty()) {
            return MailOutcome.failure("Receipt PDF not generated. Mail not sent");
        }

        List<EmailAttachment> files = new ArrayList<>();
        files.add(new EmailAttachment(rendered.get().fileName(), rendered.get().pdf(), "application/pdf"));
        List<String> skipped = new ArrayList<>();
        if (Boolean.TRUE.equals(request.getIncludeAttachments())) {
            for (AttachmentDto stored : attachments.list(AttachmentScope.of(companyId, ATTACHMENT_FOLDER, receiptId, null))) {
                Optional<byte[]> bytes = attachments.read(stored.getPath());
                if (bytes.isPresent()) {
                    files.add(new EmailAttachment(stored.getFileName(), bytes.get(), stored.getContentType()));
                } else {
                    skipped.add(stored.getFileName());
                }
            }
        }

        String subject = request.getSubject() == null || request.getSubject().isBlank()
                ? defaultSubject() : request.getSubject().trim();
        String body;
        try {
            body = renderBody(receipt, customer, request.getRemarks());
        } catch (IOException ex) {
            return MailOutcome.failure("Receipt mail template not found");
        }

        MimeMessage sent;
        try {
            sent = emailService.sendHtmlMail(to, cc, subject, body, files);
        } catch (RuntimeException ex) {
            log.error("Receipt {} mail failed", receiptNo, ex);
            return MailOutcome.failure("Mail sending failed: " + rootMessage(ex));
        }

        StringBuilder message = new StringBuilder("Receipt " + receiptNo + " mailed to " + String.join(", ", to));
        if (!cc.isEmpty()) {
            message.append(" (cc: ").append(String.join(", ", cc)).append(')');
        }
        if (!skipped.isEmpty()) {
            message.append("; missing attachment(s) skipped: ").append(String.join(", ", skipped));
        }
        // the mail is delivered at this point: a failure to file a copy is only a warning
        String warning = sentFolder.flatMap(s -> s.appendToSent(sent)).orElse(null);
        if (warning != null) {
            message.append("  (delivered, but the copy could not be placed in Sent: ").append(warning).append(')');
        }
        return new MailOutcome(true, message.toString(), to, cc, files.size(), warning);
    }

    /** The template with its tokens filled; every value is HTML-escaped. */
    String renderBody(Receipt receipt, Customer customer, String remarks) throws IOException {
        String template;
        try (InputStream in = new ClassPathResource(TEMPLATE).getInputStream()) {
            template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        String remarksBlock = remarks == null || remarks.isBlank() ? ""
                : "<p><b>Remarks:</b> " + HtmlUtils.htmlEscape(remarks.trim()) + "</p>";
        return template
                .replace("{CustomerName}", HtmlUtils.htmlEscape(customer == null ? "" : orEmpty(customer.getCustomerName())))
                .replace("{ReceiptNo}", HtmlUtils.htmlEscape(orEmpty(receipt.getCNumberDisplay())))
                .replace("{ReceiptDate}", receipt.getReceiptDate() == null ? "" : receipt.getReceiptDate().toLocalDate().format(DATE))
                .replace("{RefNumber}", HtmlUtils.htmlEscape(orEmpty(receipt.getRefNumber())))
                .replace("{Amount}", new DecimalFormat("#,##0.00").format(money(receipt.getAmount())))
                .replace("{Currency}", HtmlUtils.htmlEscape(currencySymbol(customer)))
                .replace("{RemarksBlock}", remarksBlock)
                .replace("{CURRENT_DATE}", LocalDate.now().format(LONG_DATE));
    }

    private String defaultSubject() {
        String subject = mailProperties.getReceiptSubject();
        return subject == null || subject.isBlank() ? "Payment Received - Thank You" : subject.trim();
    }

    private String currencySymbol(Customer customer) {
        if (customer == null || customer.getSymbolRefid() == null) {
            return "RM";
        }
        return symbols.findById(customer.getSymbolRefid()).map(SymbolMaster::getSName)
                .filter(s -> s != null && !s.isBlank()).map(String::trim).orElse("RM");
    }

    private Receipt find(Integer receiptId, Integer companyId) {
        if (receiptId == null || receiptId <= 0 || companyId == null || companyId <= 0) {
            return null;
        }
        Receipt receipt = receipts.findById(receiptId).orElse(null);
        return receipt == null || !Objects.equals(receipt.getCompanyRefId(), companyId) ? null : receipt;
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() == null ? ex.getClass().getSimpleName() : t.getMessage();
    }

    /** What happened, in words the screen shows. */
    public record MailOutcome(boolean ok, String message, List<String> to, List<String> cc,
                              int attachmentCount, String sentCopyWarning) {
        static MailOutcome failure(String message) {
            return new MailOutcome(false, message, List.of(), List.of(), 0, null);
        }
    }
}
