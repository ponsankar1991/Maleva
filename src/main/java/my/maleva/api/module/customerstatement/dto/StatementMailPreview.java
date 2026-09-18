package my.maleva.api.module.customerstatement.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * The statement mail exactly as Send would build it, before anything is sent:
 * the screen shows it, lets the operator change the addresses, subject and
 * wording, and posts the result back to {@code /send}.
 *
 * @param customerName   who the statement is for
 * @param reminder       the wording used: "" for the statement, else "Reminder 1" / "Reminder 2"
 * @param subject        the template subject for that wording
 * @param body           the filled HTML body (banner, figures, bank details, signature)
 * @param to             the addresses asked for, else the customer's statement addresses
 * @param cc             the configured receivables CC
 * @param attachmentName the PDF file name the mail will carry
 * @param excelAttachmentName the Excel workbook's file name, when that is attached instead or as well
 * @param overdueAmount  the figure quoted in the body
 * @param currency       its currency
 */
public record StatementMailPreview(
        String customerName,
        String reminder,
        String subject,
        String body,
        List<String> to,
        List<String> cc,
        String attachmentName,
        String excelAttachmentName,
        BigDecimal overdueAmount,
        String currency
) {
}
