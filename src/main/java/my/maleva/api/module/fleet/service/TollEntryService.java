package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.TollEntryDetailDto;
import my.maleva.api.module.fleet.dto.TollEntryListResponse;
import my.maleva.api.module.fleet.dto.request.TollEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.TollEntrySearchRequest;

/**
 * Toll entries: a header per truck and date, with one row per toll transaction
 * taken off the toll operator's statement.
 *
 * Replaces the legacy TollEntryServices. The six actions the jqx screen called
 * map onto the methods here:
 *
 * <pre>
 *   /TollEntry/SelectTollEntry   -> search
 *   /TollEntry/MaxTollEntryNo    -> nextTollNumber
 *   /TollEntry/InsertTollEntry   -> save
 *   /TollEntry/EditTollEntry     -> getForEdit
 *   /TollEntry/DeleteTollEntry   -> delete
 *   /TollEntry/TollEntryVIEW     -> getForPrint
 * </pre>
 */
public interface TollEntryService {

    /** The toll entry list with its total. Filters are all optional. */
    TollEntryListResponse search(TollEntrySearchRequest request);

    /**
     * The next toll number, formatted as the screen shows it - "TE" followed by
     * nine digits, for example {@code TE000000241}.
     *
     * <p>A preview only: the number is fixed when SP_TollEntry runs.
     */
    String nextTollNumber(Integer companyRefId);

    /**
     * Creates or updates an entry and its transactions through SP_TollEntry.
     *
     * <p>The whole detail set is replaced: the procedure deletes every existing
     * TollEntryDetails row of the master before inserting what it was given.
     */
    TollEntryDetailDto save(TollEntrySaveRequest request, String username);

    /**
     * One entry with its transactions, for the edit form.
     *
     * @param tollNumber when non-null the entry is found by printed number
     *                   instead of by id, matching the legacy TollEntryNo path
     */
    TollEntryDetailDto getForEdit(Integer id, Integer tollNumber, Integer companyRefId);

    /** Soft delete of the header. The transactions stay, orphaned as they were before. */
    void delete(Integer id, Integer companyRefId, String username);

    /** The single entry behind the print action. */
    TollEntryDetailDto getForPrint(Integer id, Integer companyRefId);
}
