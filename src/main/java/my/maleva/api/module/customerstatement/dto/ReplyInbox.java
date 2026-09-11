package my.maleva.api.module.customerstatement.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The header bell's view of customer replies nobody has opened yet.
 *
 * @param allowed whether this user's role is on the notify list; when false
 *                the count is 0 and the list empty, and the bell stays hidden
 * @param count   unread replies for the company, all customers
 * @param items   the newest first, up to the limit asked for
 */
public record ReplyInbox(boolean allowed, int count, List<Item> items) {

    public record Item(
            long replyId,
            int customerId,
            String customerName,
            String fromAddress,
            String fromName,
            String subject,
            String snippet,
            LocalDateTime receivedAt,
            /** The mail of ours it answers, so the reader knows what it is about. */
            String statementSubject,
            LocalDateTime statementSentAt
    ) {
    }
}
