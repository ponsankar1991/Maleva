package my.maleva.api.module.paymentrecept.service;

import my.maleva.api.module.paymentrecept.dto.ReceiptSaveResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stops one new receipt being created twice.
 *
 * <p>The page waits 60 s for a save. When the server is slower (the receipt
 * numbering waits on SequenceNoMaster row locks in production) the receipt is
 * committed but the page reports a failure and the form is still a new entry,
 * so pressing SAVE again - or React Query's own mutation retry - created a
 * second receipt settling the same bills.
 *
 * <p>The page sends one key per new receipt, kept across failures and replaced
 * only after a create succeeds. The first request with a key owns it; a second
 * while the first is still running is refused; a second after it committed
 * gets the <em>first result</em> back, so the page opens the receipt that was
 * really created. A failed or rolled-back create releases the key. In this JVM
 * only, like {@code SaleOrderCreateGuard}: a restart forgets the keys.
 */
@Component
public class ReceiptCreateGuard {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptCreateGuard.class);

    /** How long a finished create can still be replayed. */
    static final Duration RETENTION = Duration.ofMinutes(10);

    /** The answer to a repeat of a create that is still running. */
    public static final String STILL_SAVING_MESSAGE =
            "This receipt is still being saved. Wait a moment and press SAVE again - "
                    + "it will open the saved receipt instead of creating a second one.";

    private final Map<String, Entry> creates = new ConcurrentHashMap<>();
    private final Duration retention;

    public ReceiptCreateGuard() {
        this(RETENTION);
    }

    ReceiptCreateGuard(Duration retention) {
        this.retention = retention;
    }

    private record Entry(ReceiptSaveResponseDto result, Instant at) {
        boolean running() {
            return result == null;
        }
    }

    /** What {@link #begin} decided for a create. */
    public sealed interface Claim permits Owned, Running, Finished {
    }

    /** This caller owns the key and should create. */
    public record Owned() implements Claim {
    }

    /** The same create is still running. */
    public record Running() implements Claim {
    }

    /** The same create already committed; answer with its result. */
    public record Finished(ReceiptSaveResponseDto result) implements Claim {
    }

    public Claim begin(String key) {
        evictExpired();
        Entry existing = creates.putIfAbsent(key, new Entry(null, Instant.now()));
        if (existing == null) {
            return new Owned();
        }
        if (existing.running()) {
            logger.warn("Refused a second receipt create for key {} while the first is still running", key);
            return new Running();
        }
        logger.info("Replaying the finished receipt create for key {} as receipt {} ({})",
                key, existing.result().getId(), existing.result().getName());
        return new Finished(existing.result());
    }

    /** Records the created receipt, so a retry replays it instead of repeating it. */
    public void complete(String key, ReceiptSaveResponseDto result) {
        creates.put(key, new Entry(result, Instant.now()));
    }

    /** Releases the key after a failure, so the clerk can really try again. */
    public void abandon(String key) {
        creates.remove(key);
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(retention);
        creates.entrySet().removeIf(entry -> entry.getValue().at().isBefore(cutoff));
    }
}
