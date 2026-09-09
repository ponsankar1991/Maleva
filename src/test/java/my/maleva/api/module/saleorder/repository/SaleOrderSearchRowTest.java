package my.maleva.api.module.saleorder.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ordering of the /api/sale-orders/search result rows.
 *
 * The screen shows the rows in exactly the order the API returns them - it does no
 * client-side sorting - so this order is what the user sees.
 */
class SaleOrderSearchRowTest {

    private static SaleOrderSearchRow<String> row(String label, String saleDate, String deta, Integer id) {
        return new SaleOrderSearchRow<>(
                label,
                saleDate == null ? null : LocalDateTime.parse(saleDate),
                deta == null ? null : LocalDateTime.parse(deta),
                id);
    }

    private static List<String> sortedLabels(List<SaleOrderSearchRow<String>> rows) {
        List<SaleOrderSearchRow<String>> copy = new ArrayList<>(rows);
        copy.sort(SaleOrderSearchRow.masterOrder());
        return copy.stream().map(SaleOrderSearchRow::value).toList();
    }

    @Test
    @DisplayName("master rows come back newest sale date first")
    void ordersBySaleDateDescending() {
        List<String> ordered = sortedLabels(List.of(
                row("older", "2026-01-05T00:00", "2026-01-06T08:00", 1),
                row("newest", "2026-03-20T00:00", "2026-03-21T08:00", 2),
                row("middle", "2026-02-11T00:00", "2026-02-12T08:00", 3)));

        assertThat(ordered).containsExactly("newest", "middle", "older");
    }

    @Test
    @DisplayName("dates are compared chronologically, not as dd/MM/yyyy text")
    void doesNotCompareDatesAsText() {
        // The regression this pins: rows used to be sorted on the formatted DETA string,
        // where "31/01/2020" sorts after "01/12/2026" because the day is compared first.
        List<String> ordered = sortedLabels(List.of(
                row("jan-2020", "2020-01-31T00:00", "2020-01-31T00:00", 1),
                row("dec-2026", "2026-12-01T00:00", "2026-12-01T00:00", 2)));

        assertThat(ordered).containsExactly("dec-2026", "jan-2020");
    }

    @Test
    @DisplayName("DETA breaks a sale-date tie, then the id")
    void fallsBackToDetaThenId() {
        String sameDay = "2026-05-04T00:00";

        List<String> ordered = sortedLabels(List.of(
                row("early-eta", sameDay, "2026-05-04T06:00", 10),
                row("late-eta", sameDay, "2026-05-04T18:00", 11),
                row("no-eta-low-id", sameDay, null, 12),
                row("no-eta-high-id", sameDay, null, 13)));

        assertThat(ordered)
                .containsExactly("late-eta", "early-eta", "no-eta-high-id", "no-eta-low-id");
    }

    @Test
    @DisplayName("rows with no sale date sort last instead of leading the list")
    void missingSaleDateSortsLast() {
        List<String> ordered = sortedLabels(List.of(
                row("undated", null, null, 1),
                row("dated", "2026-01-01T00:00", null, 2)));

        assertThat(ordered).containsExactly("dated", "undated");
    }

    @Test
    @DisplayName("the order is total, so batching the id list cannot reshuffle rows")
    void orderIsIndependentOfInputOrder() {
        List<SaleOrderSearchRow<String>> rows = List.of(
                row("a", "2026-01-01T00:00", "2026-01-02T00:00", 1),
                row("b", "2026-01-01T00:00", "2026-01-03T00:00", 2),
                row("c", "2026-02-01T00:00", null, 3));

        List<SaleOrderSearchRow<String>> reversed = new ArrayList<>(rows);
        java.util.Collections.reverse(reversed);

        assertThat(sortedLabels(rows)).isEqualTo(sortedLabels(reversed));
    }

    @Test
    @DisplayName("detail rows keep the legacy SaleOrderDetails.Id order across batches")
    void detailRowsOrderById() {
        List<SaleOrderSearchRow<String>> rows = new ArrayList<>(List.of(
                new SaleOrderSearchRow<>("third", null, null, 300),
                new SaleOrderSearchRow<>("first", null, null, 100),
                new SaleOrderSearchRow<>("second", null, null, 200)));

        rows.sort(SaleOrderSearchRow.detailOrder());

        assertThat(rows.stream().map(SaleOrderSearchRow::value))
                .containsExactly("first", "second", "third");
    }
}
