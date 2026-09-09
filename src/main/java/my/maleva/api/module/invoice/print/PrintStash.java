package my.maleva.api.module.invoice.print;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds a rendered report for the moment between "the screen asked for it" and
 * "the popup fetched it".
 *
 * <p><b>Why this exists.</b> The report opens in a new window, and a new window
 * carries no {@code Authorization} header — the same reason {@code /uploads/**}
 * is already public. So the front end used to fetch the PDF with the bearer
 * token and hand the window a {@code blob:} URL. That works, but a blob URL is
 * {@code blob:http://host/73eb1fc2-9920-41d8-946a-5a81734a4ad2}, and every
 * browser names a download after the last segment of the URL. Operators were
 * saving invoices as {@code 73eb1fc2-….pdf}.
 *
 * <p>A ticket fixes the name at the source: the screen asks for the report
 * over the authenticated API, the bytes wait here under an unguessable id, and
 * the window opens a plain URL that ends in the real file name and carries
 * {@code Content-Disposition}. The browser then offers
 * {@code INV000044007.pdf}, which is what the Crystal popup always did.
 *
 * <p><b>What makes the public read safe.</b> The ticket is a random UUID that
 * only the caller who passed authentication is ever told; it dies after
 * {@link #TTL}; and it yields nothing but the bytes already rendered for that
 * caller — there is no id to tamper with, so no way to walk to another
 * invoice. Reads are repeatable on purpose (a PDF viewer may re-fetch or the
 * operator may reload) rather than single-use.
 */
@Component
public class PrintStash {

    private static final Logger log = LoggerFactory.getLogger(PrintStash.class);

    /** Long enough to open and reload the window, short enough to forget. */
    static final Duration TTL = Duration.ofMinutes(3);

    /**
     * A hard ceiling so a burst of prints cannot grow the heap without bound.
     * At a few hundred KB per invoice this is tens of MB in the worst case,
     * and entries normally leave within seconds of being collected.
     */
    private static final int MAX_ENTRIES = 100;

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    /** A rendered report waiting to be collected. */
    public record Entry(String fileName, byte[] pdf, Instant expiresAt) {
        boolean isLive(Instant now) {
            return now.isBefore(expiresAt);
        }
    }

    /**
     * Stores the rendered report and returns the ticket that reads it back.
     *
     * @param fileName the name the browser should offer, e.g. INV000044007.pdf
     */
    public String put(String fileName, byte[] pdf) {
        Instant now = Instant.now();
        evict(now);

        String ticket = UUID.randomUUID().toString();
        entries.put(ticket, new Entry(fileName, pdf, now.plus(TTL)));
        return ticket;
    }

    /** The report for this ticket, or empty when it never existed or has expired. */
    public Optional<Entry> get(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return Optional.empty();
        }
        Entry entry = entries.get(ticket);
        if (entry == null) {
            return Optional.empty();
        }
        if (!entry.isLive(Instant.now())) {
            entries.remove(ticket);
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    /**
     * Drops what has expired, then the oldest of what is left if the map is
     * still at its ceiling. Losing an unexpired entry only costs the operator
     * a second print, so a bounded map is worth more than a perfect one.
     */
    private void evict(Instant now) {
        entries.entrySet().removeIf(e -> !e.getValue().isLive(now));

        int excess = entries.size() - (MAX_ENTRIES - 1);
        if (excess <= 0) {
            return;
        }
        log.warn("Print stash is full ({} entries); dropping the {} oldest", entries.size(), excess);
        List<String> oldest = entries.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().expiresAt()))
                .limit(excess)
                .map(Map.Entry::getKey)
                .toList();
        oldest.forEach(entries::remove);
    }
}
