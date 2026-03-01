package my.maleva.api.repo;

import my.maleva.api.model.RulesTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * RulesTypeMasterRepository
 * Spring Data JPA Repository for RulesTypeMaster entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface RulesTypeMasterRepository extends JpaRepository<RulesTypeMaster, Integer> {

    /**
     * Find all RulesTypeMaster records by company ID
     */
    List<RulesTypeMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active RulesTypeMaster records by company ID
     */
    List<RulesTypeMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find RulesTypeMaster by rule type code
     */
    Optional<RulesTypeMaster> findByCompanyRefIdAndRuleTypeCode(Integer companyRefId, String ruleTypeCode);

    /**
     * Find RulesTypeMaster by rule type name
     */
    Optional<RulesTypeMaster> findByCompanyRefIdAndRuleTypeName(Integer companyRefId, String ruleTypeName);

    /**
     * Check if rule type code exists
     */
    boolean existsByCompanyRefIdAndRuleTypeCode(Integer companyRefId, String ruleTypeCode);

    /**
     * Count RulesTypeMaster by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active RulesTypeMaster by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}

