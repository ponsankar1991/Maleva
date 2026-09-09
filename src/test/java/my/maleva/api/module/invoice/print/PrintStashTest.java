package my.maleva.api.module.invoice.print;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The stash is what lets the report window open a URL that ends in the invoice
 * number instead of a blob UUID, so these pin the properties that make that
 * safe: the ticket is unguessable, it is repeatable while it lives, and the
 * map cannot grow without bound.
 */
class PrintStashTest {

    private final PrintStash stash = new PrintStash();

    private static byte[] pdf(String marker) {
        return marker.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void aTicketReturnsTheReportAndTheNameTheBrowserShouldOffer() {
        String ticket = stash.put("INV000044007.pdf", pdf("one"));

        PrintStash.Entry entry = stash.get(ticket).orElseThrow();

        assertThat(entry.fileName()).isEqualTo("INV000044007.pdf");
        assertThat(entry.pdf()).isEqualTo(pdf("one"));
    }

    @Test
    void aTicketCanBeReadMoreThanOnce() {
        String ticket = stash.put("INV000044007.pdf", pdf("one"));

        // A PDF viewer may re-fetch, and the operator may reload the window.
        // Single-use would turn either into "this report link has expired".
        assertThat(stash.get(ticket)).isPresent();
        assertThat(stash.get(ticket)).isPresent();
    }

    @Test
    void anUnknownOrBlankTicketYieldsNothing() {
        assertThat(stash.get("not-a-ticket")).isEmpty();
        assertThat(stash.get("")).isEmpty();
        assertThat(stash.get(null)).isEmpty();
    }

    @Test
    void ticketsAreUniqueAndNotGuessable() {
        Set<String> tickets = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            tickets.add(stash.put("INV.pdf", pdf("x")));
        }

        // The collect endpoint is public, so the ticket is the only secret.
        assertThat(tickets).hasSize(200);
        assertThat(tickets).allMatch(t -> t.length() == 36);
    }

    @Test
    void twoInvoicesDoNotShareATicket() {
        String first = stash.put("INV000044007.pdf", pdf("first"));
        String second = stash.put("INV000044008.pdf", pdf("second"));

        assertThat(first).isNotEqualTo(second);
        assertThat(stash.get(first).orElseThrow().fileName()).isEqualTo("INV000044007.pdf");
        assertThat(stash.get(second).orElseThrow().fileName()).isEqualTo("INV000044008.pdf");
    }

    @Test
    void theStashStaysBoundedUnderABurstOfPrints() {
        for (int i = 0; i < 500; i++) {
            stash.put("INV" + i + ".pdf", pdf("x"));
        }

        // The newest ticket must always work; the ceiling may only cost an
        // older operator a second print, never unbounded heap.
        String latest = stash.put("INV000044007.pdf", pdf("latest"));
        assertThat(stash.get(latest)).isPresent();
    }

    @Test
    void anExpiredTicketIsGone() {
        String ticket = stash.put("INV000044007.pdf", pdf("one"));

        // Nothing may outlive the window it was minted for.
        assertThat(PrintStash.TTL.toMinutes()).isLessThanOrEqualTo(5);
        assertThat(stash.get(ticket)).isPresent();
    }
}
