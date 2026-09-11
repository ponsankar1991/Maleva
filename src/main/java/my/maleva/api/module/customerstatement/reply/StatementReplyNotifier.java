package my.maleva.api.module.customerstatement.reply;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


/**
 * Tells open browsers that a customer has replied, over the STOMP broker.
 *
 * <p>The socket endpoint is open (no token on the handshake), so the frame
 * carries only a signal — the company and a count — never the customer, the
 * sender or the text. The header bell then fetches the unread list over the
 * REST API, where the token and the role check apply. Same pattern as the
 * Planning screen's "UPDATE" broadcast.
 */
@Slf4j
@Component
public class StatementReplyNotifier {

    public static final String TOPIC_PREFIX = "/topic/statement-replies/";

    private final SimpMessagingTemplate messaging;

    public StatementReplyNotifier(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /** {@code /topic/statement-replies/<companyId>}: "there are new replies, go and fetch". */
    public void newReplies(int companyId, int count) {
        try {
            // A JSON string, not a Map: convertAndSend(String, Map) is ambiguous between
            // (destination, payload) and (payload, headers), and the browser parses the body anyway.
            String payload = "{\"event\":\"NEW_REPLIES\",\"companyId\":" + companyId + ",\"count\":" + count
                    + ",\"at\":\"" + LocalDateTime.now() + "\"}";
            messaging.convertAndSend(TOPIC_PREFIX + companyId, payload);
            log.debug("Notified company {} of {} new statement repl{}", companyId, count, count == 1 ? "y" : "ies");
        } catch (RuntimeException ex) {
            // a broker hiccup must not fail the import; the bell polls anyway
            log.warn("Could not push the new-reply notification for company {}: {}", companyId, ex.getMessage());
        }
    }
}
