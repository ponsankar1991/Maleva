package my.maleva.api.repo;

import my.maleva.api.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * SupplierRepository - Repository for Supplier
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    /**
     * Find all Supplier records by company ID
     */
    List<Supplier> findByCompanyRefId(Integer companyRefId);

    /**
     * Find Supplier by name
     */
    Optional<Supplier> findBySupplierName(String supplierName);

    /**
     * Find Supplier by C Number and Company
     */
    Optional<Supplier> findByCNumberAndCompanyRefId(Integer cNumber, Integer companyRefId);

    /**
     * Find all active Supplier records by company
     */
    List<Supplier> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find all Supplier records by supplier type
     */
    List<Supplier> findBySupplierType(String supplierType);

    /**
     * Find Supplier by email
     */
    Optional<Supplier> findByEmail(String email);

    /**
     * Find all Supplier records by country
     */
    List<Supplier> findByCountry(String country);

    /**
     * Find all Supplier records by city
     */
    List<Supplier> findByCity(String city);

    /**
     * Count Supplier records by company ID
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active Supplier records by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Check if supplier name exists
     */
    boolean existsBySupplierName(String supplierName);

    /**
     * Find Supplier by GST No
     */
    Optional<Supplier> findByGstNo(String gstNo);
}

