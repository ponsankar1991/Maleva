package my.maleva.api.service;

import my.maleva.api.dto.SymbolMasterDto;
import java.util.List;
import java.util.Optional;

/**
 * SymbolMasterService - Business logic for SymbolMaster
 * Handles currency symbol management
 */
public interface SymbolMasterService {

    List<SymbolMasterDto> getByCompanyRefId(Integer companyRefId);

    List<SymbolMasterDto> getActiveByCompanyRefId(Integer companyRefId);

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

