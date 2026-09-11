package my.maleva.api.module.customerstatement.reply;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A mail read from the accounts mailbox — the headers first (cheap, fetched
 * for every new mail), the body only for the ones that turn out to be a
 * customer's reply.
 *
 * @param uid        the IMAP UID inside its folder; with the folder's
 *                   UIDVALIDITY, the mail's stable identity
 * @param messageId  the mail's own Message-ID, angle brackets included
 * @param inReplyTo  the Message-ID it answers, if the client set one
 * @param references the thread's Message-IDs, oldest first, if set
 */
public record InboundMail(
        long uid,
        String messageId,
        String inReplyTo,
        List<String> references,
        String fromAddress,
        String fromName,
        String to,
        String cc,
        String subject,
        LocalDateTime receivedAt
) {
    /** The parsed content: the readable text, the HTML if any, and the files attached. */
    public record Body(String text, String html, List<Attachment> attachments) {
    }

    public record Attachment(String fileName, String contentType, byte[] content) {
    }
}
