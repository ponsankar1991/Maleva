package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.FuelEntryDetailDto;
import my.maleva.api.module.fleet.dto.FuelEntryListResponse;
import my.maleva.api.module.fleet.dto.request.FuelEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.FuelEntrySearchRequest;

/**
 * Fuel entries: what a truck was fuelled with, and how that compares to what
 * the GPS fuel sensor saw.
 *
 * Replaces the legacy FuelEntryServices. The six actions the jqx screen called
 * map onto the methods here:
 *
 * <pre>
 *   /FuelEntry/SelectFuelEntry   -> search
 *   /FuelEntry/MaxFuelEntryNo    -> nextFuelNumber
 *   /FuelEntry/InsertFuelEntry   -> save
 *   /FuelEntry/EditFuelEntry     -> getForEdit
 *   /FuelEntry/DeleteFuelEntry   -> delete
 *   /FuelEntry/FuelEntryVIEW     -> getForPrint
 * </pre>
 */
public interface FuelEntryService {

    /**
     * The fuel entry list, with the totals shown beside it.
     * Filters are all optional; see {@link FuelEntrySearchRequest}.
     */
    FuelEntryListResponse search(FuelEntrySearchRequest request);

    /**
     * The next fuel number, formatted as the screen shows it - "FE" followed by
     * nine digits, for example {@code FE000005194}.
     *
     * <p>Reserves nothing: this is a preview, and the number is only fixed when
     * SP_FuelEntry runs. Two people opening the form at once therefore see the
     * same number, exactly as they did in the legacy screen.
     */
    String nextFuelNumber(Integer companyRefId);

    /**
     * Creates or updates an entry through SP_FuelEntry.
     *
     * @return the saved entry, reloaded so the caller sees the number the
     *         stored procedure assigned
     */
    FuelEntryDetailDto save(FuelEntrySaveRequest request, String username);

    /**
     * One entry with its matched GPS filling, for the edit form.
     *
     * @param fuelNumber when non-null the entry is found by printed number
     *                   instead of by id, matching the legacy FuelEntryNo path
     */
    FuelEntryDetailDto getForEdit(Integer id, Integer fuelNumber, Integer companyRefId);

    /** Soft delete. {@code mobile} restricts it to driver-app rows, as the legacy call did. */
    void delete(Integer id, Integer companyRefId, boolean mobile, String username);

    /** The single entry behind the print action. */
    FuelEntryDetailDto getForPrint(Integer id, Integer companyRefId);
}
