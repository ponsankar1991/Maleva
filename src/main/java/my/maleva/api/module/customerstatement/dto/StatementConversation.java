package my.maleva.api.module.customerstatement.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Everything exchanged with one customer about their statement, oldest
 * first: what we sent (statements and answers, from the send log) and what
 * they wrote back (from the mailbox).
 *
 * @param emails            the customer master's statement addresses, the default for a fresh mail
 * @param previousAddresses addresses earlier mails went to that are not on the record — the one-off
 *                          or temporary ones the operator typed, offered back so they need not be retyped
 */
public record StatementConversation(
        int customerId,
        String customerName,
        List<String> emails,
        List<String> previousAddresses,
        List<Entry> entries
) {
    /**
     * @param kind         SENT (ours) or REPLY (theirs)
     * @param body         readable text; a reply's HTML is never sent to the screen
     * @param status       for SENT: SENT or FAILED
     * @param unread       for REPLY: nobody has opened it in the app yet
     * @param replyAllHint for REPLY: the addresses Reply All would go to, for the compose preview
     */
    public record Entry(
            String kind,
            long id,
            LocalDateTime at,
            String from,
            String to,
            String cc,
            String subject,
            String body,
            String status,
            String error,
            String sentBy,
            String attachmentName,
            boolean unread,
            List<Attachment> attachments,
            List<String> replyAllHint
    ) {
    }

    public record Attachment(long id, String fileName, String contentType, long sizeBytes) {
    }
}
