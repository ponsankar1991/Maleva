package my.maleva.api.module.master.service;

import my.maleva.api.module.master.dto.SymbolMasterDto;
import java.util.List;
import java.util.Optional;

/**
 * SymbolMasterService - Business logic for SymbolMaster
 * Handles currency symbol management
 */
public interface SymbolMasterService {

    List<SymbolMasterDto> getByCompanyRefId(Integer companyRefId);

    List<SymbolMasterDto> getActiveByCompanyRefId(Integer companyRefId);

    /**
     * Select symbols for company (equivalent to .NET SelectSymbol)
     * Returns all non-deleted symbols (Active != 2)
     * Used for dropdowns, selection lists, and UI displays
     *
     * @param companyRefId The company ID
     * @return List of SymbolMasterDto records where Active != 2
     */
    List<SymbolMasterDto> selectSymbol(Integer companyRefId);

    Optional<SymbolMasterDto> getBySName(String sName, Integer companyRefId);

    Optional<SymbolMasterDto> getByCName(String cName);

    List<SymbolMasterDto> getByDFlag(Integer dFlag);

    List<SymbolMasterDto> getByQneId(Integer qneId);

    Optional<SymbolMasterDto> getById(Integer id);

    SymbolMasterDto create(SymbolMasterDto dto);

    SymbolMasterDto update(Integer id, SymbolMasterDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countActiveByCompanyRefId(Integer companyRefId);

    void validateSymbolMasterData(SymbolMasterDto dto);

    SymbolMasterDto activateSymbol(Integer id);

    SymbolMasterDto deactivateSymbol(Integer id);

    boolean existsBySName(String sName, Integer companyRefId);

    SymbolMasterDto processSymbol(SymbolMasterDto dto, Integer companyId, Integer checkFlag);
}

