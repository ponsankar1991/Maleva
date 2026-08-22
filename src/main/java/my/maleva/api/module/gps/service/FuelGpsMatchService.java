package my.maleva.api.module.gps.service;

import my.maleva.api.module.gps.dto.GpsFuelMatchDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Pairs the GPS fuel fillings of a truck with the fuel entries keyed in for the
 * same day.
 *
 * This is the bridge the whole FuelEntry screen rests on: the entered litres
 * (Aliter) are compared against the litres the on-board sensor actually saw
 * (Gliter), and the difference is the fuel variance the report exists to show.
 *
 * <p>Two rules hold the result steady:
 *
 * <ul>
 *   <li>a filling already stored on {@code FuelEntry.FuelFillingRefId} is
 *       honoured, so an answer does not change once it has been decided;</li>
 *   <li>any remaining filling goes to at most one entry. The legacy query ran a
 *       separate {@code TOP 1 ... ORDER BY closest} per entry with nothing to
 *       stop two entries choosing the same filling: 60 entries across 29
 *       collisions share one, and in the worst case two entries with identical
 *       litres both claimed the same 57 litre filling.</li>
 * </ul>
 */
public interface FuelGpsMatchService {

    /**
     * Matches every active fuel entry of one truck on one day. Read-only.
     *
     * @return one row per fuel entry, in entry id order; entries with no filling
     *         carry a null fuelFillingId and an unmatchedReason
     */
    List<GpsFuelMatchDto> matchForTruckOnDay(Integer companyRefId,
                                             Integer truckRefId,
                                             LocalDate saleDate);

    /**
     * Runs the match and writes the result to {@code FuelFillingRefId}, so the
     * pairing stops being recomputed on every read.
     *
     * <p>Entries already carrying a MANUAL link are left alone.
     *
     * @return how many entries had their link written or changed
     */
    int persistAutoMatches(Integer companyRefId, Integer truckRefId, LocalDate saleDate);

    /**
     * Pins one entry to one filling by hand, for the days where the litres alone
     * cannot say which filling belongs to which entry.
     *
     * <p>Passing a null {@code fuelFillingId} clears the link and lets the
     * matcher decide again.
     *
     * @throws my.maleva.api.common.exception.InvalidRequestException when the
     *         filling belongs to a different truck or day than the entry, or is
     *         already held by another entry
     */
    GpsFuelMatchDto setManualMatch(Integer companyRefId, Integer fuelEntryId, Integer fuelFillingId);
}
