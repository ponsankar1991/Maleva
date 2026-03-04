package my.maleva.api.repo;

import my.maleva.api.model.TaxMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TaxMasterRepository - Repository for TaxMaster
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface TaxMasterRepository extends JpaRepository<TaxMaster, Integer> {

    /**
     * Find all TaxMaster records by company ID
     */
    List<TaxMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all active TaxMaster records by company
     */
    List<TaxMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find TaxMaster by code and company
     */
    Optional<TaxMaster> findByCodeAndCompanyRefId(String code, Integer companyRefId);

    /**
     * Find TaxMaster by description and company with active status
     */
    Optional<TaxMaster> findByDescriptionAndCompanyRefIdAndActive(String description, Integer companyRefId, Integer active);

    /**
     * Find all TaxMaster records by tax IO type
     */
    List<TaxMaster> findByTaxIO(Integer taxIO);

    /**
     * Find all TaxMaster records by company and tax IO
     */
    List<TaxMaster> findByCompanyRefIdAndTaxIO(Integer companyRefId, Integer taxIO);

    /**
     * Count TaxMaster records by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active TaxMaster records by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Check if tax code exists for company
     */
    boolean existsByCodeAndCompanyRefId(String code, Integer companyRefId);

    /**
     * Find all TaxMaster records by company ID excluding deleted (Active != 2)
     */
    List<TaxMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);
}

