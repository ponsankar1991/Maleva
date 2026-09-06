package my.maleva.api.module.common.service;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMessage;
import my.maleva.api.common.config.MailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Files a copy of a mail that has already been sent into the mailbox's Sent
 * folder over IMAP — the port of legacy {@code commonfunctions.AppendToSentFolder}.
 *
 * <p>A plain SMTP send never puts a copy in Sent, which is why sent receipts
 * were missing from the mailbox even though customers received them. The
 * bean only exists when {@code mail.imap.host} is set; the mail is already
 * delivered by the time this runs, so a failure here is reported as a warning
 * and must never turn the send into a failure.
 */
@Service
@ConditionalOnProperty(prefix = "mail.imap", name = "host")
public class ImapSentFolderService {

    private static final Logger log = LoggerFactory.getLogger(ImapSentFolderService.class);
    private static final int DEFAULT_IMAPS_PORT = 993;
    private static final List<String> USUAL_SENT_FOLDERS = List.of("Sent", "INBOX.Sent", "Sent Items", "[Gmail]/Sent Mail");

    private final MailProperties properties;

    public ImapSentFolderService(MailProperties properties) {
        this.properties = properties;
        log.info("IMAP Sent-folder copies enabled: host={}", properties.getImap().getHost());
    }

    /**
     * Appends {@code message} to the Sent folder.
     *
     * @return empty on success, otherwise the reason so the caller can warn
     */
    public Optional<String> appendToSent(MimeMessage message) {
        MailProperties.Imap imap = properties.getImap();
        if (imap == null || imap.getHost() == null || imap.getHost().isBlank()) {
            return Optional.of("IMAP host not configured");
        }
        int port = imap.getPort() != null ? imap.getPort() : DEFAULT_IMAPS_PORT;
        String user = imap.getUsername() != null ? imap.getUsername() : properties.getSmtp().getUsername();
        String password = imap.getPassword() != null ? imap.getPassword() : properties.getSmtp().getPassword();

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", imap.getHost());
        props.put("mail.imaps.port", String.valueOf(port));
        props.put("mail.imaps.connectiontimeout", "30000");
        props.put("mail.imaps.timeout", "30000");
        Session session = Session.getInstance(props);
        try (Store store = session.getStore("imaps")) {
            store.connect(imap.getHost(), port, user, password);
            Folder sent = findSentFolder(store, imap.getSentFolder());
            if (sent == null) {
                return Optional.of("no Sent folder found in the mailbox");
            }
            try {
                sent.open(Folder.READ_WRITE);
                message.setFlag(Flags.Flag.SEEN, true);
                sent.appendMessages(new Message[]{message});
                log.info("Sent copy filed in IMAP folder '{}'", sent.getFullName());
                return Optional.empty();
            } finally {
                if (sent.isOpen()) {
                    sent.close(false);
                }
            }
        } catch (Exception ex) {
            log.warn("Could not file the sent copy in the IMAP Sent folder: {}", ex.getMessage());
            return Optional.of(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        }
    }

    /** The configured folder when given, else the first of the usual names that exists. */
    private static Folder findSentFolder(Store store, String configured) throws Exception {
        List<String> candidates = new ArrayList<>();
        if (configured != null && !configured.isBlank()) {
            candidates.add(configured.trim());
        }
        candidates.addAll(USUAL_SENT_FOLDERS);
        for (String name : candidates) {
            Folder folder = store.getFolder(name);
            if (folder != null && folder.exists()) {
                return folder;
            }
        }
        return null;
    }
}
