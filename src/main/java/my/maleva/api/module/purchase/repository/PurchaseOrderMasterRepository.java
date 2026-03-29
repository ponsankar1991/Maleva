package my.maleva.api.module.purchase.repository;

import my.maleva.api.module.purchase.entity.PurchaseOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PurchaseOrderMasterRepository
 * Spring Data JPA Repository for PurchaseOrderMaster entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface PurchaseOrderMasterRepository extends JpaRepository<PurchaseOrderMaster, Integer> {

    /**
     * Find all PurchaseOrderMaster records by company ID
     */
    List<PurchaseOrderMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active PurchaseOrderMaster records by company ID
     */
    List<PurchaseOrderMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find PurchaseOrderMaster by invoice number
     */
    Optional<PurchaseOrderMaster> findByCompanyRefIdAndInvoiceNo(Integer companyRefId, String invoiceNo);

    /**
     * Find PurchaseOrderMaster by supplier
     */
    List<PurchaseOrderMaster> findByCompanyRefIdAndSupplierRefId(Integer companyRefId, Integer supplierRefId);

    /**
     * Find PurchaseOrderMaster by sale type
     */
    List<PurchaseOrderMaster> findByCompanyRefIdAndSaleType(Integer companyRefId, String saleType);

    /**
     * Find PurchaseOrderMaster by date range
     */
    List<PurchaseOrderMaster> findByCompanyRefIdAndSaleDateBetween(Integer companyRefId, LocalDate startDate, LocalDate endDate);

    /**
     * Find PurchaseOrderMaster by employee
     */
    List<PurchaseOrderMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);

    /**
     * Find PurchaseOrderMaster by CNumber
     */
    Optional<PurchaseOrderMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Check if CNumber exists
     */
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Count PurchaseOrderMaster by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active PurchaseOrderMaster by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}

