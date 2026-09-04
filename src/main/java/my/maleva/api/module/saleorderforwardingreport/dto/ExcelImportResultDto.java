package my.maleva.api.module.saleorderforwardingreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * What the forwarding Excel import actually did, row by row.
 *
 * <p>This is the fix for the legacy import's worst habit. `ProcessExcel` looped
 * the sheet and `continue`d past every row it could not use — no MY number in
 * the mark column, no matching sale order, all three forwarding legs already
 * filled — and past every row that threw, logging those server-side only. It
 * then returned "Excel Forwarding Updated Successfully" regardless, so a file
 * that changed nothing at all was indistinguishable from one that worked.
 *
 * <p>Every row now lands in exactly one bucket and says why, so the screen can
 * show the operator what happened to their file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResultDto {

    /** Data rows examined — the sheet's row count less the header. */
    private int totalRows;

    /** Rows that updated a sale order. */
    private int updatedCount;

    /** Rows deliberately passed over; see {@link Outcome#reason}. */
    private int skippedCount;

    /** Rows that threw. The import continues past these, as legacy did. */
    private int failedCount;

    /**
     * Per-row detail, in sheet order.
     *
     * <p>Capped by the service so a 50,000-row sheet cannot turn one response
     * into a multi-megabyte payload; {@link #detailsTruncated} says when that
     * happened. The counts above always cover the whole sheet.
     */
    private List<Outcome> details;

    /** True when {@link #details} holds only the first N rows. */
    private boolean detailsTruncated;

    /** One row's fate. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Outcome {

        /** 1-based row number as it appears in Excel, so it can be pointed at. */
        private int rowNumber;

        /** The MY number extracted from the mark column, when there was one. */
        private String jobNumber;

        private Status status;

        /**
         * Why, in words the operator can act on: "no MY number in column CK",
         * "job MY12345 not found", "all three forwarding legs already filled".
         * Null when the row simply worked.
         */
        private String reason;

        /** Which forwarding leg was written: 1, 2 or 3. Null unless updated. */
        private Integer fwNo;
    }

    public enum Status {
        UPDATED,
        SKIPPED,
        FAILED
    }
}
