package my.maleva.api.integration.qne;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One push at a time per document.
 *
 * <p>QNE takes minutes to answer a create. The browser gives up long before
 * that, the operator sees a timeout, assumes nothing happened and clicks
 * again — while the first call is still open. The only dedup the integration
 * has is "QNECode is empty", and QNECode is written from the <em>response</em>,
 * so during those minutes it is still empty and the second click takes the
 * create branch as well. QNE ends up with two invoices for one job. That is
 * the duplicate the operators have been reporting.
 *
 * <p>This closes that window: the second push is refused while the first is
 * still running, instead of quietly creating a second document. Once the
 * first finishes, QNECode is set and the ordinary create/update branch takes
 * over, so a later retry updates rather than duplicates.
 *
 * <p>Two deliberate limits:
 * <ul>
 *   <li><b>In this JVM only.</b> A second server instance would not see these
 *       keys. The alternative, a database guard, cannot be used here: Hikari
 *       runs {@code SET NOCOUNT ON}, so every UPDATE reports -1 rows and a
 *       compare-and-set cannot tell "I claimed it" from "someone else had it";
 *       and {@code sp_getapplock} is transaction-scoped, while this call must
 *       stay outside a transaction — holding one open for the length of a QNE
 *       call is what the after-commit pushes exist to avoid.</li>
 *   <li><b>Entries expire.</b> A push that hangs would otherwise hold its key
 *       for ever. Anything older than the client's own read timeout is treated
 *       as abandoned and can be claimed again, so a genuine retry is never
 *       blocked permanently.</li>
 * </ul>
 */
@Component
public class QnePushLock {

    private static final Logger logger = LoggerFactory.getLogger(QnePushLock.class);

    /**
     * Longer than {@link QneClient#READ_TIMEOUT}, so a call that is merely slow
     * still holds its key while a call that can no longer be running does not.
     */
    static final Duration STALE_AFTER = QneClient.READ_TIMEOUT.plusMinutes(5);

    private final Map<String, Instant> inFlight = new ConcurrentHashMap<>();
    private final Duration staleAfter;

    public QnePushLock() {
        this(STALE_AFTER);
    }

    QnePushLock(Duration staleAfter) {
        this.staleAfter = staleAfter;
    }

    /**
     * @param key what is being pushed, e.g. {@code "sale-invoice:43933"}
     * @return true when the caller now owns the push and must
     *         {@link #release(String)} it in a finally block; false when
     *         another push for the same document is still running
     */
    public boolean tryAcquire(String key) {
        Instant now = Instant.now();
        AtomicBoolean acquired = new AtomicBoolean(false);
        inFlight.compute(key, (ignored, startedAt) -> {
            if (startedAt == null) {
                acquired.set(true);
                return now;
            }
            if (Duration.between(startedAt, now).compareTo(staleAfter) > 0) {
                logger.warn("QNE push {} started at {} never finished; letting a new push take over",
                        key, startedAt);
                acquired.set(true);
                return now;
            }
            return startedAt;
        });
        if (!acquired.get()) {
            logger.warn("Refused a second QNE push for {} while the first is still running", key);
        }
        return acquired.get();
    }

    public void release(String key) {
        inFlight.remove(key);
    }

    /** Returns true if a push is currently in flight for this key. */
    public boolean isHeld(String key) {
        return inFlight.containsKey(key);
    }

    /** How long the in-flight push for this key has been running, for messages and tests. */
    public Duration runningFor(String key) {
        Instant startedAt = inFlight.get(key);
        return startedAt == null ? Duration.ZERO : Duration.between(startedAt, Instant.now());
    }
}
