package my.maleva.api.service;

import my.maleva.api.dto.SubExpenseMasterDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SubExpenseMasterService - Business logic for SubExpenseMaster
 * Incorporates SP_SubExpense stored procedure logic
 */
public interface SubExpenseMasterService {

    List<SubExpenseMasterDto> getByCompanyRefId(Integer companyRefId);

    List<SubExpenseMasterDto> getByExpenseMasterRefId(Integer expenseMasterRefId);

    List<SubExpenseMasterDto> getByCompanyAndExpenseMaster(Integer companyRefId, Integer expenseMasterRefId);

    List<SubExpenseMasterDto> getActiveByCompany(Integer companyRefId);

    List<SubExpenseMasterDto> getByAccountRefid(Integer accountRefid);

    List<SubExpenseMasterDto> getByGlAccountRefId(Integer glAccountRefId);

    Optional<SubExpenseMasterDto> getByDescriptionAndCompany(String description, Integer companyRefId);

    Optional<SubExpenseMasterDto> getById(Integer id);

    SubExpenseMasterDto create(SubExpenseMasterDto dto);

    SubExpenseMasterDto update(Integer id, SubExpenseMasterDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countActiveByCompany(Integer companyRefId);

    void validateSubExpenseMasterData(SubExpenseMasterDto dto);

    SubExpenseMasterDto activateSubExpense(Integer id);

    SubExpenseMasterDto deactivateSubExpense(Integer id);

    SubExpenseMasterDto processSubExpense(SubExpenseMasterDto dto, Integer companyId);
}

