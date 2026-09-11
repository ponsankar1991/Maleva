package my.maleva.api.module.customerstatement.dto;

import java.time.LocalDateTime;
import java.util.List;

/** What went out when an answer was sent from the screen. */
public record ReplySendResult(
        String subject,
        List<String> sentTo,
        List<String> cc,
        String attachmentName,
        LocalDateTime sentAt
) {
}
