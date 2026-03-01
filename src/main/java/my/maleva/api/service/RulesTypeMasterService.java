package my.maleva.api.service;

import my.maleva.api.dto.RulesTypeMasterDto;
import java.util.List;
import java.util.Optional;

/**
 * RulesTypeMasterService
 * Business logic interface for RulesTypeMaster operations
 */
public interface RulesTypeMasterService {

    /**
     * Get all RulesTypeMaster records by company ID
     */
    List<RulesTypeMasterDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get active RulesTypeMaster records by company ID
     */
    List<RulesTypeMasterDto> getActiveByCompanyId(Integer companyRefId);

    /**
     * Get RulesTypeMaster by ID
     */
    Optional<RulesTypeMasterDto> getById(Integer id);

    /**
     * Create new RulesTypeMaster record
     */
    RulesTypeMasterDto create(RulesTypeMasterDto dto);

    /**
     * Update RulesTypeMaster record
     */
    RulesTypeMasterDto update(Integer id, RulesTypeMasterDto dto);

    /**
     * Delete RulesTypeMaster record
     */
    boolean delete(Integer id);

    /**
     * Get RulesTypeMaster by rule type code
     */
    Optional<RulesTypeMasterDto> getByRuleTypeCode(Integer companyRefId, String ruleTypeCode);

    /**
     * Get RulesTypeMaster by rule type name
     */
    Optional<RulesTypeMasterDto> getByRuleTypeName(Integer companyRefId, String ruleTypeName);

    /**
     * Check if rule type code exists
     */
    boolean existsByRuleTypeCode(Integer companyRefId, String ruleTypeCode);

    /**
     * Count RulesTypeMaster by company
     */
    long countByCompanyId(Integer companyRefId);

    /**
     * Count active RulesTypeMaster by company
     */
    long countActiveByCompanyId(Integer companyRefId);

    /**
     * Activate RulesTypeMaster
     */
    RulesTypeMasterDto activate(Integer id);

    /**
     * Deactivate RulesTypeMaster
     */
    RulesTypeMasterDto deactivate(Integer id);
}

