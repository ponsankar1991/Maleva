package my.maleva.api.module.customerstatement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * What went out, so the screen can say so. Legacy swallowed every mail
 * failure into a log line and reported {@code ok: true} regardless.
 *
 * @param customerName   who the statement was for
 * @param subject        the subject line actually used
 * @param sentTo         the To addresses, after trimming and de-duplication
 * @param cc             the CC addresses from configuration
 * @param attachmentName the PDF's file name
 * @param overdueAmount  the figure quoted in the mail body
 * @param currency       its currency
 * @param sentAt         server time of the send
 */
public record StatementSendResult(
        String customerName,
        String subject,
        List<String> sentTo,
        List<String> cc,
        String attachmentName,
        BigDecimal overdueAmount,
        String currency,
        LocalDateTime sentAt
) {
}
