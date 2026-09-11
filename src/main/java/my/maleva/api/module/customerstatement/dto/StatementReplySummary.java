package my.maleva.api.module.customerstatement.dto;

import java.time.LocalDateTime;

/**
 * Per customer: the latest reply and how many there are — the Send
 * Statements screen's "Reply" column and its "Replied" / "Unread" filters.
 */
public record StatementReplySummary(
        int customerId,
        int replyCount,
        int unreadCount,
        long lastReplyId,
        LocalDateTime lastReplyAt,
        String lastFrom,
        String lastFromName,
        String lastSubject,
        String lastSnippet
) {
}
