package my.maleva.api.module.expense.repository;

import my.maleva.api.module.expense.entity.SubExpenseMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SubExpenseMasterRepository - Repository for SubExpenseMaster
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SubExpenseMasterRepository extends JpaRepository<SubExpenseMaster, Integer> {

    /**
     * Find all SubExpenseMaster records by company ID
     */
    List<SubExpenseMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all SubExpenseMaster records by expense master ID
     */
    List<SubExpenseMaster> findByExpenseMasterRefId(Integer expenseMasterRefId);

    /**
     * Find all SubExpenseMaster records by company and expense master
     */
    List<SubExpenseMaster> findByCompanyRefIdAndExpenseMasterRefId(Integer companyRefId, Integer expenseMasterRefId);

    /**
     * Find all active SubExpenseMaster records by company
     */
    List<SubExpenseMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find all SubExpenseMaster records by account reference
     */
    List<SubExpenseMaster> findByAccountRefid(Integer accountRefid);

    /**
     * Find SubExpenseMaster by description and company
     */
    Optional<SubExpenseMaster> findByDescriptionAndCompanyRefId(String description, Integer companyRefId);

    /**
     * Find SubExpenseMaster records by GL account
     */
    List<SubExpenseMaster> findByGlAccountRefId(Integer glAccountRefId);

    /**
     * Count SubExpenseMaster records by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active SubExpenseMaster records by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}

