package my.maleva.api.module.customerstatement.reply;

import jakarta.mail.FetchProfile;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.ReceivedDateTerm;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.MailProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Reads new mail from the accounts mailbox over IMAP — the other half of the
 * Sent-folder copy in {@code ImapSentFolderService}, same host and login.
 *
 * <p>Two-pass on purpose: every new mail's headers are fetched in one round
 * trip (envelope, UID, the three threading headers); only a mail the caller
 * recognises as a customer's reply has its body and attachments pulled.
 * The inbox carries far more than statement replies.
 *
 * <p>Position is kept by UID: the folder's UIDVALIDITY plus the highest UID
 * seen. If the mailbox resets its UIDs (UIDVALIDITY changes), or on the first
 * run, the last {@code initialDays} days are scanned instead of the whole
 * history. Mail is only read; nothing is flagged, moved or deleted.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "mail.imap", name = "host")
public class MailboxReader {

    private static final int DEFAULT_IMAPS_PORT = 993;
    /** A reply's attachment bigger than this is noted but not stored. */
    static final long MAX_ATTACHMENT_BYTES = 15L * 1024 * 1024;

    private final MailProperties properties;

    public MailboxReader(MailProperties properties) {
        this.properties = properties;
    }

    /** What a scan covered: the folder's UIDVALIDITY, the highest UID seen, how many mails were looked at. */
    public record Scan(long uidValidity, long maxUid, int scanned) {
    }

    /** Called for every new mail, inside the open session; the body is fetched only if asked for. */
    public interface Handler {
        void handle(InboundMail mail, Supplier<InboundMail.Body> body) throws Exception;
    }

    /** The login the mailbox is read with — what the sync state and the replies are keyed by. */
    public String mailboxName() {
        MailProperties.Imap imap = properties.getImap();
        String user = imap != null && imap.getUsername() != null ? imap.getUsername() : properties.getSmtp().getUsername();
        return user == null ? "" : user.trim();
    }

    /**
     * Walk the new mail in {@code folderName}.
     *
     * @param knownValidity the UIDVALIDITY of the last scan, or null on the first
     * @param lastUid       the highest UID of the last scan
     * @param initialDays   how far back a first (or reset) scan looks
     * @param maxMessages   at most this many mails per scan; the rest wait for the next
     */
    public Scan scan(String folderName, Long knownValidity, long lastUid, int initialDays, int maxMessages,
                     Handler handler) throws Exception {
        MailProperties.Imap imap = properties.getImap();
        if (imap == null || imap.getHost() == null || imap.getHost().isBlank()) {
            throw new IllegalStateException("IMAP host not configured (mail.imap.host)");
        }
        int port = imap.getPort() != null ? imap.getPort() : DEFAULT_IMAPS_PORT;
        String user = mailboxName();
        String password = imap.getPassword() != null ? imap.getPassword() : properties.getSmtp().getPassword();

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", imap.getHost());
        props.put("mail.imaps.port", String.valueOf(port));
        props.put("mail.imaps.connectiontimeout", "30000");
        props.put("mail.imaps.timeout", "60000");
        props.put("mail.imaps.partialfetch", "false");
        Session session = Session.getInstance(props);

        try (Store store = session.getStore("imaps")) {
            store.connect(imap.getHost(), port, user, password);
            Folder folder = store.getFolder(folderName == null || folderName.isBlank() ? "INBOX" : folderName);
            if (folder == null || !folder.exists()) {
                throw new IllegalStateException("Mailbox folder '" + folderName + "' does not exist");
            }
            folder.open(Folder.READ_ONLY);
            try {
                UIDFolder uids = (UIDFolder) folder;
                long validity = uids.getUIDValidity();
                boolean fresh = knownValidity == null || knownValidity != validity;

                Message[] messages;
                if (fresh) {
                    Date since = Date.from(Instant.now().minus(Math.max(1, initialDays), ChronoUnit.DAYS));
                    messages = folder.search(new ReceivedDateTerm(ComparisonTerm.GE, since));
                    log.info("Mailbox {}/{}: first scan (UIDVALIDITY {}), {} mail(s) in the last {} day(s)",
                            user, folder.getFullName(), validity, messages.length, initialDays);
                } else {
                    // "n:*" always includes the highest UID even when it is <= n; filtered below
                    messages = uids.getMessagesByUID(lastUid + 1, UIDFolder.LASTUID);
                }

                FetchProfile profile = new FetchProfile();
                profile.add(FetchProfile.Item.ENVELOPE);
                profile.add(UIDFolder.FetchProfileItem.UID);
                profile.add("Message-ID");
                profile.add("In-Reply-To");
                profile.add("References");
                folder.fetch(messages, profile);

                long maxUid = fresh ? 0 : lastUid;
                int scanned = 0;
                for (Message message : messages) {
                    long uid = uids.getUID(message);
                    if (!fresh && uid <= lastUid) {
                        continue;
                    }
                    if (scanned >= maxMessages) {
                        log.info("Mailbox {}: {} mail(s) looked at this scan, the rest next time", user, scanned);
                        break;
                    }
                    scanned++;
                    InboundMail head = headers(message, uid);
                    handler.handle(head, () -> body(message));
                    maxUid = Math.max(maxUid, uid);
                }
                return new Scan(validity, maxUid, scanned);
            } finally {
                if (folder.isOpen()) {
                    folder.close(false);
                }
            }
        }
    }

    static InboundMail headers(Message message, long uid) throws MessagingException {
        String from = null, fromName = null;
        if (message.getFrom() != null && message.getFrom().length > 0 && message.getFrom()[0] instanceof InternetAddress ia) {
            from = ia.getAddress();
            fromName = decode(ia.getPersonal());
        }
        Date received = message.getReceivedDate() != null ? message.getReceivedDate() : message.getSentDate();
        LocalDateTime at = received == null ? LocalDateTime.now()
                : LocalDateTime.ofInstant(received.toInstant(), ZoneId.systemDefault());
        return new InboundMail(uid,
                first(message.getHeader("Message-ID")),
                first(message.getHeader("In-Reply-To")),
                split(first(message.getHeader("References"))),
                from, fromName,
                addresses(message.getRecipients(Message.RecipientType.TO)),
                addresses(message.getRecipients(Message.RecipientType.CC)),
                decode(message.getSubject()), at);
    }

    /** The readable text, the HTML if any, and the attachments (inline images included). */
    static InboundMail.Body body(Part root) {
        StringBuilder text = new StringBuilder();
        StringBuilder html = new StringBuilder();
        List<InboundMail.Attachment> attachments = new ArrayList<>();
        try {
            walk(root, text, html, attachments);
        } catch (Exception ex) {
            log.warn("Could not read a mail body fully: {}", ex.getMessage());
        }
        return new InboundMail.Body(text.length() == 0 ? null : text.toString().trim(),
                html.length() == 0 ? null : html.toString().trim(), attachments);
    }

    private static void walk(Part part, StringBuilder text, StringBuilder html, List<InboundMail.Attachment> attachments)
            throws Exception {
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                walk(multipart.getBodyPart(i), text, html, attachments);
            }
            return;
        }
        String fileName = decode(part.getFileName());
        boolean attachment = Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
                || (fileName != null && !fileName.isBlank() && !part.isMimeType("text/*"))
                || (fileName != null && !fileName.isBlank() && Part.INLINE.equalsIgnoreCase(part.getDisposition()));
        if (attachment) {
            int size = part.getSize();
            if (size > MAX_ATTACHMENT_BYTES) {
                log.info("Attachment {} ({} bytes) skipped: over the {} MB limit", fileName, size, MAX_ATTACHMENT_BYTES / 1024 / 1024);
                return;
            }
            try (InputStream in = part.getInputStream()) {
                byte[] bytes = in.readNBytes((int) MAX_ATTACHMENT_BYTES + 1);
                if (bytes.length > MAX_ATTACHMENT_BYTES) {
                    log.info("Attachment {} skipped: over the {} MB limit", fileName, MAX_ATTACHMENT_BYTES / 1024 / 1024);
                    return;
                }
                attachments.add(new InboundMail.Attachment(fileName == null ? "attachment" : fileName, baseType(part.getContentType()), bytes));
            }
            return;
        }
        if (part.isMimeType("text/plain")) {
            text.append(String.valueOf(part.getContent())).append('\n');
        } else if (part.isMimeType("text/html")) {
            html.append(String.valueOf(part.getContent())).append('\n');
        } else if (part.isMimeType("message/rfc822")) {
            Object nested = part.getContent();
            if (nested instanceof Part p) {
                walk(p, text, html, attachments);
            }
        }
    }

    private static String first(String[] values) {
        return values == null || values.length == 0 || values[0] == null ? null : values[0].trim();
    }

    private static List<String> split(String references) {
        if (references == null || references.isBlank()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (String s : references.trim().split("\\s+")) {
            if (!s.isBlank()) {
                out.add(s.trim());
            }
        }
        return out;
    }

    private static String addresses(jakarta.mail.Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        List<String> out = new ArrayList<>();
        for (jakarta.mail.Address a : addresses) {
            out.add(a instanceof InternetAddress ia ? ia.getAddress() : a.toString());
        }
        return String.join(", ", out);
    }

    private static String baseType(String contentType) {
        if (contentType == null) {
            return null;
        }
        int semicolon = contentType.indexOf(';');
        return (semicolon > 0 ? contentType.substring(0, semicolon) : contentType).trim().toLowerCase();
    }

    private static String decode(String header) {
        if (header == null) {
            return null;
        }
        try {
            return MimeUtility.decodeText(header);
        } catch (Exception ex) {
            return header;
        }
    }
}
