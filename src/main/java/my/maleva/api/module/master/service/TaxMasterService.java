package my.maleva.api.module.master.service;

import my.maleva.api.module.master.dto.TaxMasterDto;
import java.util.List;
import java.util.Optional;

/**
 * TaxMasterService - Business logic for TaxMaster
 * Incorporates SP_TaxMaster stored procedure logic
 */
public interface TaxMasterService {

    List<TaxMasterDto> getByCompanyRefId(Integer companyRefId);

    List<TaxMasterDto> getActiveByCompanyRefId(Integer companyRefId);

    Optional<TaxMasterDto> getByCode(String code, Integer companyRefId);

    Optional<TaxMasterDto> getByDescription(String description, Integer companyRefId);

    List<TaxMasterDto> getByTaxIO(Integer taxIO);

    List<TaxMasterDto> getByCompanyAndTaxIO(Integer companyRefId, Integer taxIO);

    Optional<TaxMasterDto> getById(Integer id);

    TaxMasterDto create(TaxMasterDto dto);

    TaxMasterDto update(Integer id, TaxMasterDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countActiveByCompanyRefId(Integer companyRefId);

    void validateTaxMasterData(TaxMasterDto dto);

    TaxMasterDto activateTax(Integer id);

    TaxMasterDto deactivateTax(Integer id);

    boolean existsByCode(String code, Integer companyRefId);

    TaxMasterDto processTaxMaster(TaxMasterDto dto, Integer companyId, Integer checkFlag);

    /**
     * Get all TaxMaster records by company (excluding deleted - Active != 2)
     * Equivalent to: SELECT * FROM TaxMaster WHERE CompanyRefId = ? AND Active != 2
     */
    List<TaxMasterDto> selectTax(Integer companyId);
}

