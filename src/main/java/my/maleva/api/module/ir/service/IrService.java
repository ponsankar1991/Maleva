package my.maleva.api.module.ir.service;

import my.maleva.api.module.ir.dto.IrDepartmentOptionDto;
import my.maleva.api.module.ir.dto.IrDetailDto;
import my.maleva.api.module.ir.dto.IrListResponse;
import my.maleva.api.module.ir.dto.IrSaveRequest;
import my.maleva.api.module.ir.dto.IrSearchRequest;
import my.maleva.api.module.ir.dto.IrStatusOptionDto;

import java.util.List;

public interface IrService {

    /** The filtered list with its total. */
    IrListResponse search(IrSearchRequest request);

    /** One IR for the edit form; company-scoped, soft-deleted rows read as missing. */
    IrDetailDto getById(Integer id, Integer companyRefId);

    /** Inserts when the request has no id, updates that row otherwise. */
    IrDetailDto save(IrSaveRequest request, String username);

    /** Soft delete: Active becomes 2, the row stays for audit. */
    void delete(Integer id, Integer companyRefId, String username);

    /** Status dropdown for one company. */
    List<IrStatusOptionDto> statuses(Integer companyRefId);

    /** Department dropdown, built from the UserRoles enum. */
    List<IrDepartmentOptionDto> departments();
}
