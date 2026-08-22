package my.maleva.api.module.gps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outcome of syncing one Wialon report.
 *
 * The legacy job swallowed every failure into an empty catch block, so a broken
 * report looked exactly like a report with no data. Each report now reports its
 * own status and counts, and one failure does not abort the others.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpsReportSyncResult {

    /** fuel, speed or engine. */
    private String report;

    /** OK, SKIPPED or FAILED. */
    private String status;

    /** Leaf rows returned by Wialon. */
    private int rowsFetched;

    /** Rows written to the database. */
    private int rowsWritten;

    /** Rows dropped because no active TruckMaster row matched the unit name. */
    private int rowsSkippedNoTruck;

    /** Rows dropped because the timestamp was blank or unparseable. */
    private int rowsSkippedNoTime;

    /** Rows dropped because the event count was zero, as the legacy job did. */
    private int rowsSkippedZeroCount;

    /** Failure reason when status is FAILED, or the skip reason when SKIPPED. */
    private String message;
}
