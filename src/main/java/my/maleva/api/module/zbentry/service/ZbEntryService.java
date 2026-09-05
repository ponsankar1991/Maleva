package my.maleva.api.module.zbentry.service;

import my.maleva.api.module.zbentry.dto.ZbEntryBulkSaveRequest;
import my.maleva.api.module.zbentry.dto.ZbEntryResponse;
import my.maleva.api.module.zbentry.dto.ZbEntrySaveResult;
import my.maleva.api.module.zbentry.dto.ZbEntrySearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** ZB entries: the list, one entry for editing, and the save. */
public interface ZbEntryService {

    /** The list screen: a page of entries in a date range. */
    Page<ZbEntryResponse> searchZbEntries(ZbEntrySearchRequest request, Pageable pageable);

    /**
     * One entry, for the form to load when opened with an id.
     *
     * <p>New here. Legacy had no way to fetch a single row: the form called
     * {@code SelectZBDetails} with the id stuffed into its {@code Keyword}
     * parameter — which that action never reads — and then filtered the returned
     * list client-side. Worse, it sent no date range, and the query wraps the
     * range in {@code CONVERT(date, '', 120)}, which SQL Server resolves to
     * 1900-01-01; the search therefore ran {@code BETWEEN 1900-01-01 AND
     * 1900-01-01} and came back empty, leaving the form to index into an empty
     * array. Opening a ZB entry for edit from a link has been broken.
     *
     * @throws my.maleva.api.common.exception.EntityNotFoundException when no such
     *         entry belongs to that company
     */
    ZbEntryResponse getZbEntry(Integer id, Integer companyRefId);

    /**
     * Insert or update entries through {@code SP_ZBEntryMaster}.
     *
     * <p>Rows with {@code id} null or 0 are inserted, the rest updated. The
     * procedure decides which, and runs the batch in one transaction.
     *
     * @return the procedure's own result, including the saved row's id so the
     *         caller can file attachments against a brand-new entry
     */
    ZbEntrySaveResult bulkSaveZbEntries(ZbEntryBulkSaveRequest request);
}
