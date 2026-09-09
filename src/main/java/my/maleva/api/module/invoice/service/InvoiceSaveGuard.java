package my.maleva.api.module.invoice.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.dto.SaleInvoiceSaveResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stops one Save turning into two invoices.
 *
 * <p>An invoice save writes SaleMaster, every SaleDetails row, the
 * SaleMasterReference rows and the InvoiceNo stamp on each sale order. It
 * takes long enough that an operator who does not see anything happen presses
 * Save again — and a second create is a second invoice, with its own number,
 * for the same job. Disabling the button in the browser does not cover a
 * double press before React re-renders, the F1 shortcut, a second tab, or a
 * request the network retried.
 *
 * <p>So the screen sends a key with the save — one value for one logical save,
 * kept across retries and replaced only once a save succeeds. The first
 * request with a key owns it. A second request while the first is still
 * running is refused. A second request after it finished is answered with the
 * <em>first result</em>, so a retry after a dropped reply hands back the
 * invoice that was actually created instead of creating another.
 *
 * <p>Completion is recorded after the transaction commits, never before: a
 * save that rolls back releases its key so the operator can genuinely try
 * again.
 *
 * <p>In this JVM only, which matches how this runs today. A second instance
 * would need the keys in a shared store — the same limit as
 * {@code QnePushLock}, and the same reason.
 */
@Component
public class InvoiceSaveGuard {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceSaveGuard.class);

    /**
     * How long a finished save can still be replayed.
     *
     * <p>Long enough to cover a retry by a person who walked away and came
     * back; short enough that the map cannot grow without bound.
     */
    static final Duration RETENTION = Duration.ofMinutes(10);

    private final Map<String, Entry> saves = new ConcurrentHashMap<>();
    private final Duration retention;

    public InvoiceSaveGuard() {
        this(RETENTION);
    }

    InvoiceSaveGuard(Duration retention) {
        this.retention = retention;
    }

    private record Entry(SaleInvoiceSaveResult result, Instant at) {
        boolean running() {
            return result == null;
        }
    }

    /**
     * Claims the key for this save.
     *
     * @return the result of an identical save that already finished, so the
     *         caller can return it unchanged; empty when this caller now owns
     *         the key and should carry on and save
     * @throws InvalidRequestException when the same save is still running
     */
    public Optional<SaleInvoiceSaveResult> begin(String key) {
        evictExpired();
        Entry existing = saves.putIfAbsent(key, new Entry(null, Instant.now()));
        if (existing == null) {
            return Optional.empty();
        }
        if (existing.running()) {
            logger.warn("Refused a second save for key {} while the first is still running", key);
            throw new InvalidRequestException(
                    "This invoice is already being saved. Wait for it to finish — "
                            + "saving again would create a second invoice.");
        }
        logger.info("Replaying the finished save for key {} as invoice {}", key, existing.result().getBillNo());
        return Optional.of(existing.result());
    }

    /** Records what the save produced, so a retry replays it instead of repeating it. */
    public void complete(String key, SaleInvoiceSaveResult result) {
        saves.put(key, new Entry(result, Instant.now()));
    }

    /** Releases the key after a failure, so the operator can really try again. */
    public void abandon(String key) {
        saves.remove(key);
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(retention);
        saves.entrySet().removeIf(entry -> entry.getValue().at().isBefore(cutoff));
    }
}
