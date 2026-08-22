package my.maleva.api.module.gps.service;

import my.maleva.api.module.gps.dto.GpsSyncResultDto;

import java.time.LocalDateTime;

/**
 * Pulls fuel fillings, speeding events and engine hours from Wialon into the
 * local tables.
 *
 * Replaces the legacy Quartz job Common/GPSJob.cs. Behaviour kept from it:
 * the same three reports, the same 12 hour default window, the same
 * delete-then-insert upsert keyed on truck plus timestamp, and the same
 * CompanyRefId stamped on every row.
 */
public interface GpsSyncService {

    /**
     * Syncs the default window - now minus {@code wialon.window-hours} up to now.
     * Never throws: a failing report is reported in the result instead.
     */
    GpsSyncResultDto sync();

    /**
     * Syncs an explicit window, for backfilling a gap.
     *
     * @param from start of the interval, inclusive
     * @param to   end of the interval, inclusive
     */
    GpsSyncResultDto sync(LocalDateTime from, LocalDateTime to);
}
