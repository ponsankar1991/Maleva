package my.maleva.api.module.supplier.dto;

import java.util.List;

/**
 * One page of the SupplierView grid.
 *
 * @param items the rows of this page
 * @param total how many suppliers match the search across all pages
 * @param page  zero-based page number that was served
 * @param size  page size that was served (after clamping)
 */
public record SupplierGridPage(List<SupplierListRow> items, long total, int page, int size) {
}
