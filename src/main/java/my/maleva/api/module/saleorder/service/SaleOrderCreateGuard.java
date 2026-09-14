package my.maleva.api.module.saleorder.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.saleorder.dto.SaleOrderMasterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stops one new sale order being created twice.
 *
 * <p>The page waits a fixed time for a create. When the server is slower, the
 * order is saved but the page reports a failure and still treats the form as
 * new, so pressing Save again created a second order with its own job number.
 *
 * <p>The page now sends one key per new order, kept across failures and replaced
 * only after a create succeeds. The first request with a key owns it. A second
 * one while the first is still running is refused. A second one after it
 * finished gets the <em>first result</em> back, so the retry opens the order that
 * was really created instead of creating another.
 *
 * <p>Completion is recorded after the transaction commits; a rollback releases
 * the key. In this JVM only, like {@code InvoiceSaveGuard}: a restart forgets
 * the keys, and a second instance would need them in a shared store.
 */
@Component
public class SaleOrderCreateGuard {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderCreateGuard.class);

    /** How long a finished create can still be replayed. */
    static final Duration RETENTION = Duration.ofMinutes(10);

    private final Map<String, Entry> creates = new ConcurrentHashMap<>();
    private final Duration retention;

    public SaleOrderCreateGuard() {
        this(RETENTION);
    }

    SaleOrderCreateGuard(Duration retention) {
        this.retention = retention;
    }

    private record Entry(SaleOrderMasterDto result, Instant at) {
        boolean running() {
            return result == null;
        }
    }

    /**
     * Claims the key for this create.
     *
     * @return the order an identical create already made, to return unchanged;
     *         empty when this caller now owns the key and should create
     * @throws InvalidRequestException when the same create is still running
     */
    public Optional<SaleOrderMasterDto> begin(String key) {
        evictExpired();
        Entry existing = creates.putIfAbsent(key, new Entry(null, Instant.now()));
        if (existing == null) {
            return Optional.empty();
        }
        if (existing.running()) {
            logger.warn("Refused a second create for key {} while the first is still running", key);
            throw new InvalidRequestException(
                    "This sale order is still being saved. Wait a moment and press Save again — "
                            + "it will open the saved order instead of creating a second one.");
        }
        logger.info("Replaying the finished create for key {} as sale order {} ({})",
                key, existing.result().getId(), existing.result().getCNumberDisplay());
        return Optional.of(existing.result());
    }

    /** Records the created order, so a retry replays it instead of repeating it. */
    public void complete(String key, SaleOrderMasterDto result) {
        creates.put(key, new Entry(result, Instant.now()));
    }

    /** Releases the key after a failure, so the operator can really try again. */
    public void abandon(String key) {
        creates.remove(key);
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(retention);
        creates.entrySet().removeIf(entry -> entry.getValue().at().isBefore(cutoff));
    }
}
