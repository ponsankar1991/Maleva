package my.maleva.api.module.saleorder.repository;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * A SelectSaleOrder result row together with the keys it is ordered by.
 *
 * The keys are deliberately kept off the view model: they are the underlying
 * datetimes, while the view model carries the dd/MM/yyyy strings the screen shows.
 * Sorting the displayed strings is what used to put 31/01/2020 after 01/12/2026.
 *
 * @param value     the row handed to the caller
 * @param saleDate  SaleOrderMaster.SaleDate
 * @param deta      ETA falling back to OETA - the legacy DETA sort key
 * @param tieBreak  row id, so the order is total and stable across id batches
 */
record SaleOrderSearchRow<T>(T value,
                             LocalDateTime saleDate,
                             LocalDateTime deta,
                             Integer tieBreak) {

    /**
     * Master rows: SaleDate descending, then DETA, then id - newest job first.
     *
     * Note this is not the legacy order, which was {@code OrderBy(DETA).ThenBy(BillDate)}
     * ascending. Rows with no date sort last rather than leading the list.
     */
    static <T> Comparator<SaleOrderSearchRow<T>> masterOrder() {
        return Comparator
                .comparing(SaleOrderSearchRow<T>::saleDate, newestFirst())
                .thenComparing(SaleOrderSearchRow<T>::deta, newestFirst())
                .thenComparing(SaleOrderSearchRow<T>::tieBreak,
                        Comparator.nullsLast(Comparator.reverseOrder()));
    }

    /**
     * Detail rows: SaleOrderDetails.Id, as the legacy query ordered them. Applied in
     * Java because the ids are queried in batches, so no single statement sees them all.
     */
    static <T> Comparator<SaleOrderSearchRow<T>> detailOrder() {
        return Comparator.comparing(SaleOrderSearchRow<T>::tieBreak,
                Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    private static Comparator<LocalDateTime> newestFirst() {
        return Comparator.nullsLast(Comparator.reverseOrder());
    }
}
