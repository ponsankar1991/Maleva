package my.maleva.api.module.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * EmailService - Handles all email operations
 * Replaces .NET email functionality from commonfunctions class
 *
 * Features:
 * - HTML email with templates
 * - Attachment handling (images, PDFs, documents)
 * - Multiple recipient support
 * - CC/BCC support
 *
 * Note: Email functionality is optional. If mail.smtp.host is not configured,
 * the service will skip email operations and log warnings instead.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final Optional<JavaMailSender> mailSender;
    private final RestTemplate restTemplate;
    private boolean emailConfigured = false;

    @Value("${mail.from.email:admin@maleva.my}")
    private String fromEmail;

    @Value("${mail.from.name:MALEVA}")
    private String fromName;

    @Value("${server.host:https://maleva.my/}")
    private String serverHost;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender, RestTemplate restTemplate) {
        this.mailSender = Optional.ofNullable(mailSender);
        this.restTemplate = restTemplate;
        this.emailConfigured = mailSender != null;

        if (!emailConfigured) {
            logger.warn("⚠️  Email service is not configured. Mail operations will be skipped. " +
                    "To enable email, configure 'mail.smtp.host' in application.yaml");
        }
    }

    /**
     * Send status update email with images and HTML template
     * Equivalent to OrderMail and TESTMail methods
     */
    public void sendStatusUpdateEmail(List<String> imageUrls, String status,
                                     String jobNo, String type, String recipientEmail) {
        if (!emailConfigured) {
            logger.warn("Email not configured. Skipping status update email for job: {}", jobNo);
            return;
        }
        try {
            String htmlBody = buildStatusUpdateEmailBody(imageUrls, status, jobNo, type);
            sendHtmlEmail(recipientEmail, "Status Updated - " + jobNo, htmlBody, imageUrls);
            logger.info("Status update email sent successfully - Job: {}, Type: {}", jobNo, type);
        } catch (Exception ex) {
            logger.error("Error sending status update email for job: {}", jobNo, ex);
        }
    }

    /**
     * Send invoice creation email
     * Equivalent to OrderMail2 and TESTMail3 methods
     */
    public void sendInvoiceEmail(List<String> attachmentUrls, String invoiceNo,
                                String customerName, String employeeName, List<String> recipients) {
        if (!emailConfigured) {
            logger.warn("Email not configured. Skipping invoice email for invoice: {}", invoiceNo);
            return;
        }
        try {
            String htmlBody = buildInvoiceEmailBody(invoiceNo, customerName, employeeName);
            for (String recipient : recipients) {
                sendHtmlEmail(recipient, "Invoice Created - " + invoiceNo, htmlBody, attachmentUrls);
            }
            logger.info("Invoice email sent successfully - Invoice: {}", invoiceNo);
        } catch (Exception ex) {
            logger.error("Error sending invoice email for invoice: {}", invoiceNo, ex);
        }
    }

    /** Whether a mail server is configured; nothing is sent when it is not. */
    public boolean isConfigured() {
        return emailConfigured;
    }

    /** A file to attach, already in memory. */
    public record EmailAttachment(String fileName, byte[] content, String contentType) {
    }

    /**
     * Sends the "Invoice Created" mail with in-memory attachments to every
     * recipient in one message — the port of {@code OrderMail2 + TESTMail3}.
     * Unlike the URL-based sender above this one reports failure: the Share
     * button needs to tell the operator when nothing went out.
     *
     * @throws IllegalStateException when the mail server is not configured or refuses the message
     */
    public void sendInvoiceMail(List<String> recipients, String invoiceNo, String customerName,
                                String employeeName, List<EmailAttachment> attachments) {
        if (!emailConfigured) {
            throw new IllegalStateException("Mail server is not configured (mail.smtp.host)");
        }
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalStateException("No recipients");
        }
        try {
            MimeMessage message = mailSender.get().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("Invoice Created - " + invoiceNo);
            helper.setText(buildInvoiceEmailBody(invoiceNo, customerName, employeeName), true);
            for (EmailAttachment attachment : attachments == null ? List.<EmailAttachment>of() : attachments) {
                if (attachment == null || attachment.content() == null || attachment.content().length == 0) {
                    continue;
                }
                String type = attachment.contentType() == null || attachment.contentType().isBlank()
                        ? "application/octet-stream" : attachment.contentType();
                helper.addAttachment(attachment.fileName(),
                        () -> new java.io.ByteArrayInputStream(attachment.content()), type);
            }
            mailSender.get().send(message);
            logger.info("Invoice mail sent - Invoice: {} to {} recipient(s)", invoiceNo, recipients.size());
        } catch (MessagingException | IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * Sends one HTML mail with explicit To and CC lists and in-memory
     * attachments, and hands back the message that went out so a caller can
     * file a copy of exactly that message (see {@code ImapSentFolderService}).
     *
     * <p>Unlike the fire-and-forget senders above this reports failure: the
     * Receipt screen shows the operator whether the customer got the mail.
     *
     * @throws IllegalStateException when the mail server is not configured, no
     *                               To address was given, or the server refuses the message
     */
    public MimeMessage sendHtmlMail(List<String> to, List<String> cc, String subject, String htmlBody,
                                    List<EmailAttachment> attachments) {
        if (!emailConfigured) {
            throw new IllegalStateException("Mail server is not configured (mail.smtp.host)");
        }
        if (to == null || to.isEmpty()) {
            throw new IllegalStateException("No recipients");
        }
        try {
            MimeMessage message = mailSender.get().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to.toArray(new String[0]));
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc.toArray(new String[0]));
            }
            helper.setSubject(subject == null ? "" : subject);
            helper.setText(htmlBody == null ? "" : htmlBody, true);
            for (EmailAttachment attachment : attachments == null ? List.<EmailAttachment>of() : attachments) {
                if (attachment == null || attachment.content() == null || attachment.content().length == 0) {
                    continue;
                }
                String type = attachment.contentType() == null || attachment.contentType().isBlank()
                        ? "application/octet-stream" : attachment.contentType();
                helper.addAttachment(attachment.fileName(),
                        () -> new java.io.ByteArrayInputStream(attachment.content()), type);
            }
            mailSender.get().send(message);
            logger.info("Mail '{}' sent to {} recipient(s), {} cc", subject, to.size(), cc == null ? 0 : cc.size());
            return message;
        } catch (MessagingException | IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * Send customer statement email
     * Equivalent to GenerateAndSendCustomerStatement + CustomerStamentMail methods
     */
    public void sendCustomerStatementEmail(String customerName, String emailIds,
                                          String overdueDate, String overdueAmount,
                                          String pdfPath, String reminder) {
        if (!emailConfigured) {
            logger.warn("Email not configured. Skipping customer statement email for customer: {}", customerName);
            return;
        }
        try {
            String htmlBody = buildCustomerStatementEmailBody(customerName, overdueDate,
                    overdueAmount, reminder);

            MimeMessage message = mailSender.get().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(emailIds.split(","));
            helper.setCc("receivable@maleva.com.my");
            helper.setSubject("Statement of Account & Payment Request - " + customerName);
            helper.setText(htmlBody, true);

            // Attach PDF if exists
            if (pdfPath != null && new File(pdfPath).exists()) {
                helper.addAttachment(new File(pdfPath).getName(), new File(pdfPath));
            }

            mailSender.get().send(message);
            logger.info("Customer statement email sent successfully - Customer: {}", customerName);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            logger.error("Error sending customer statement email for customer: {}", customerName, ex);
        }
    }

    /**
     * Send custom message email with attachments
     * Equivalent to OrderStatusUpdate + TESTMailStatus methods
     */
    public void sendCustomMessageEmail(List<String> attachmentUrls, String jobNo,
                                      String htmlMessageBody, String recipientEmail) {
        if (!emailConfigured) {
            logger.warn("Email not configured. Skipping custom message email for job: {}", jobNo);
            return;
        }
        try {
            sendHtmlEmail(recipientEmail, "Status Updated - " + jobNo, htmlMessageBody, attachmentUrls);
            logger.info("Custom message email sent successfully - Job: {}", jobNo);
        } catch (Exception ex) {
            logger.error("Error sending custom message email for job: {}", jobNo, ex);
        }
    }

    /**
     * Core method to send HTML email with attachments
     */
    private void sendHtmlEmail(String recipientEmail, String subject, String htmlBody,
                              List<String> attachmentUrls) throws MessagingException, IOException, UnsupportedEncodingException {
        if (!emailConfigured) {
            logger.warn("Email not configured. Cannot send email to: {}", recipientEmail);
            return;
        }

        MimeMessage message = mailSender.get().createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        // Add attachments from URLs
        if (attachmentUrls != null && !attachmentUrls.isEmpty()) {
            for (int i = 0; i < attachmentUrls.size(); i++) {
                String urlString = attachmentUrls.get(i);
                if (urlString != null && !urlString.isEmpty()) {
                    try {
                        addAttachmentFromUrl(helper, urlString, i);
                    } catch (Exception ex) {
                        logger.warn("Failed to attach file from URL: {}", urlString, ex);
                    }
                }
            }
        }

        mailSender.get().send(message);
    }

    /**
     * Add attachment from URL
     */
    private void addAttachmentFromUrl(MimeMessageHelper helper, String urlString, int index)
            throws IOException, MessagingException {
        URL url = new URL(urlString);
        String filename = extractFilenameFromUrl(urlString, index);

        // Download file content
        byte[] fileContent = restTemplate.getForObject(urlString, byte[].class);
        if (fileContent != null) {
            helper.addAttachment(filename, () -> new java.io.ByteArrayInputStream(fileContent));
        }
    }

    /**
     * Extract filename from URL with extension
     */
    private String extractFilenameFromUrl(String urlString, int index) {
        String extension = ".jpeg";
        if (urlString.contains(".pdf")) {
            extension = ".pdf";
        } else if (urlString.contains(".png")) {
            extension = ".png";
        } else if (urlString.contains(".jpg") || urlString.contains(".jpeg")) {
            extension = ".jpeg";
        }
        return "attachment_" + index + extension;
    }

    /**
     * Build HTML email body for status update
     */
    private String buildStatusUpdateEmailBody(List<String> imageUrls, String status,
                                             String jobNo, String type) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<table style='width:100%;'>");
        html.append("<tr><td>");
        html.append("<img src='").append(serverHost).append("Content/images/pngimages/logo.png' ");
        html.append("style='height: auto;width: 70%;margin: 5px;' />");
        html.append("</td></tr>");
        html.append("<tr><td><h2>").append(type).append(" Status Updated</h2></td></tr>");
        html.append("<tr><td><p><strong>Job No:</strong> ").append(jobNo).append("</p></td></tr>");
        html.append("<tr><td><p><strong>Status:</strong> ").append(status).append("</p></td></tr>");

        // Add images
        if (imageUrls != null && !imageUrls.isEmpty()) {
            html.append("<tr><td><h3>Attachments:</h3></td></tr>");
            for (String imageUrl : imageUrls) {
                html.append("<tr><td>");
                html.append("<img src='").append(imageUrl).append("' ");
                html.append("style='height: auto;width: 70%;margin: 5px;' />");
                html.append("</td></tr>");
            }
        }

        html.append("<tr><td><hr></td></tr>");
        html.append("<tr><td>");
        html.append("<p>No 20-1 JLK MPPMU 1 MEDAN PERNIAGAAN,70300 Seremban,<br>");
        html.append("Negeri Sembilan, Malaysia. Tel: 012-290 7151 & 012-241 7151</p>");
        html.append("<p>URL : www.maleva.com.my | Email : operation@maleva.com.my</p>");
        html.append("</td></tr>");
        html.append("</table>");
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Build HTML email body for invoice
     */
    private String buildInvoiceEmailBody(String invoiceNo, String customerName, String employeeName) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<table style='width:100%;'>");
        html.append("<tr><td>");
        html.append("<img src='").append(serverHost).append("Content/images/pngimages/MalevaBanner.png' ");
        html.append("style='width:100%; max-width:650px; margin-bottom:10px;' />");
        html.append("</td></tr>");
        html.append("<tr><td><h2>Invoice Created</h2></td></tr>");
        html.append("<tr><td><p><strong>Invoice No:</strong> ").append(invoiceNo).append("</p></td></tr>");
        html.append("<tr><td><p><strong>Customer Name:</strong> ").append(customerName).append("</p></td></tr>");
        html.append("<tr><td><p><strong>Handled By:</strong> ").append(employeeName).append("</p></td></tr>");
        html.append("<tr><td><hr></td></tr>");
        html.append("</table>");
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Build HTML email body for customer statement
     */
    private String buildCustomerStatementEmailBody(String customerName, String overdueDate,
                                                  String overdueAmount, String reminder) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<table style='width:100%;'>");
        html.append("<tr><td>");
        html.append("<img src='").append(serverHost).append("Content/images/pngimages/MalevaBanner.png' ");
        html.append("style='width:100%; max-width:650px; margin-bottom:10px;' />");
        html.append("</td></tr>");
        html.append("<tr><td><h2>Statement of Account</h2></td></tr>");
        html.append("<tr><td><p><strong>Customer Name:</strong> ").append(customerName).append("</p></td></tr>");
        html.append("<tr><td><p><strong>Overdue Amount:</strong> ").append(overdueAmount).append("</p></td></tr>");

        if (reminder != null && !reminder.isEmpty()) {
            html.append("<tr><td><p><strong>Reminder:</strong></p>");
            html.append("<p>").append(reminder).append("</p></td></tr>");
        }

        html.append("<tr><td><hr></td></tr>");
        html.append("</table>");
        html.append("</body></html>");
        return html.toString();
    }

    /**
     * Get content type by file extension
     */
    public static String getContentType(String extension) {
        switch (extension.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".pdf":
                return "application/pdf";
            case ".xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }
}

