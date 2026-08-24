package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.PassEntryDetailDto;
import my.maleva.api.module.fleet.dto.PassEntryListResponse;
import my.maleva.api.module.fleet.dto.RtiOptionDto;
import my.maleva.api.module.fleet.dto.request.PassEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.PassEntrySearchRequest;

import java.util.List;

/**
 * The operations a truck pass screen needs, shared by levi and auto pass
 * entries.
 *
 * Their legacy .NET services were byte-identical apart from the document number
 * prefix, so the contract - and the implementation in
 * {@link my.maleva.api.module.fleet.service.impl.AbstractPassEntryService} - is
 * declared once. {@link LeviEntryService} and {@link AutoPassEntryService}
 * extend this only so each screen's bean can be injected by its own type.
 */
public interface PassEntryService {

    /** The filtered list with its total. Legacy: {@code Select<Doc>}. */
    PassEntryListResponse search(PassEntrySearchRequest request);

    /**
     * Next document number for a blank form, e.g. {@code LE000000241} or
     * {@code AP000000167}. Legacy: {@code Max<Doc>No}.
     */
    String nextNumber(Integer companyRefId);

    /**
     * One entry for the edit form.
     *
     * @param documentNumber when set, resolves the entry by its printed sequence
     *                       number instead of {@code id}
     */
    PassEntryDetailDto getForEdit(Integer id, Integer documentNumber, Integer companyRefId);

    /** The data behind the print action. Legacy: {@code <Doc>VIEW}. */
    PassEntryDetailDto getForPrint(Integer id, Integer companyRefId);

    /** Creates or updates an entry through its stored procedure. Legacy: {@code Insert<Doc>}. */
    PassEntryDetailDto save(PassEntrySaveRequest request, String username);

    /** Soft delete. Legacy: {@code Delete<Doc>}. */
    void delete(Integer id, Integer companyRefId, String username);

    /** Options for the screen's RTI dropdown. Legacy: {@code SelectRTINo}. */
    List<RtiOptionDto> rtiOptions(Integer companyRefId);
}
