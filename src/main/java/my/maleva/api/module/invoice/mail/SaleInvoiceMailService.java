package my.maleva.api.module.invoice.mail;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.config.MailProperties;
import my.maleva.api.module.common.service.EmailService;
import my.maleva.api.module.common.service.EmailService.EmailAttachment;
import my.maleva.api.module.filehandling.dto.AttachmentDto;
import my.maleva.api.module.filehandling.model.AttachmentScope;
import my.maleva.api.module.filehandling.service.AttachmentStorageService;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.print.SaleInvoicePdfService;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The Share button on the Sale Invoice view — the port of the legacy
 * {@code SaleInvoiceAppController.MailInvoice}.
 *
 * <p>Legacy pushed the invoice to QNE, exported the Crystal report to disk,
 * and mailed that file plus the invoice's attachments to a fixed list of
 * company addresses hard-coded in {@code commonfunctions.TESTMail3}. Here
 * the PDF is the Jasper print rendered in memory, the attachments are read
 * from storage, and the recipients come from {@code mail.invoice-recipients}.
 * The QNE push is not repeated: legacy only needed it to get the report's
 * data, and the QNE button remains its own action.
 */
@Service
@RequiredArgsConstructor
public class SaleInvoiceMailService {

    private static final Logger log = LoggerFactory.getLogger(SaleInvoiceMailService.class);

    /** Storage folder the legacy screen used for invoice attachments. */
    static final String ATTACHMENT_FOLDER = "SaleInvoice";

    private final SaleMasterRepository saleMasters;
    private final SaleInvoicePdfService pdfService;
    private final AttachmentStorageService attachments;
    private final EmailService emailService;
    private final MailProperties mailProperties;

    /** Sends the invoice; the outcome carries a message the operator can act on. */
    public MailOutcome send(Integer invoiceId, Integer companyId, String employeeName) {
        if (invoiceId == null || invoiceId <= 0 || companyId == null || companyId <= 0) {
            return MailOutcome.failure("Invoice and company are required");
        }
        List<String> recipients = mailProperties.getInvoiceRecipients() == null ? List.of()
                : mailProperties.getInvoiceRecipients().stream()
                        .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (recipients.isEmpty()) {
            return MailOutcome.failure("No invoice mail recipients are configured (mail.invoice-recipients)");
        }
        if (!emailService.isConfigured()) {
            return MailOutcome.failure("The mail server is not configured on this server (mail.smtp.host)");
        }

        SaleMaster invoice = saleMasters.findById(invoiceId).orElse(null);
        if (invoice == null || !Objects.equals(invoice.getCompanyRefId(), companyId)) {
            return MailOutcome.failure("Invoice " + invoiceId + " was not found for this company");
        }

        Optional<SaleInvoicePdfService.RenderedInvoice> rendered = pdfService.render(invoiceId, companyId);
        if (rendered.isEmpty()) {
            return MailOutcome.failure("Invoice " + invoiceId + " could not be printed");
        }

        List<EmailAttachment> files = new ArrayList<>();
        files.add(new EmailAttachment(rendered.get().fileName(), rendered.get().pdf(), "application/pdf"));
        List<String> skipped = new ArrayList<>();
        for (AttachmentDto stored : attachments.list(AttachmentScope.of(companyId, ATTACHMENT_FOLDER, invoiceId, null))) {
            Optional<byte[]> bytes = attachments.read(stored.getPath());
            if (bytes.isPresent()) {
                files.add(new EmailAttachment(stored.getFileName(), bytes.get(), stored.getContentType()));
            } else {
                skipped.add(stored.getFileName());
            }
        }

        String invoiceNo = invoice.getCNumberDisplay() == null ? String.valueOf(invoiceId) : invoice.getCNumberDisplay();
        try {
            // legacy addressed every invoice mail to "Sir/Mam"; the body template is unchanged
            emailService.sendInvoiceMail(recipients, invoiceNo, "Sir/Mam",
                    employeeName == null ? "" : employeeName, files);
        } catch (RuntimeException ex) {
            log.error("Invoice {} mail failed", invoiceNo, ex);
            return MailOutcome.failure("Invoice " + invoiceNo + " could not be sent: " + rootMessage(ex));
        }

        String message = "Invoice " + invoiceNo + " sent to " + recipients.size() + " recipient(s)"
                + (skipped.isEmpty() ? "" : "; missing attachment(s) skipped: " + String.join(", ", skipped));
        return new MailOutcome(true, message, recipients, files.size());
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() == null ? ex.getClass().getSimpleName() : t.getMessage();
    }

    /** What happened, in words the screen shows. */
    public record MailOutcome(boolean ok, String message, List<String> recipients, int attachmentCount) {
        static MailOutcome failure(String message) {
            return new MailOutcome(false, message, List.of(), 0);
        }
    }
}
