package my.maleva.api.module.purchase.repository;

import my.maleva.api.module.purchase.entity.PurchaseMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseMasterRepository extends JpaRepository<PurchaseMaster, Integer> {

    /**
     * Find all PurchaseMaster records by company ID
     */
    List<PurchaseMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active PurchaseMaster records by company ID
     */
    List<PurchaseMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find PurchaseMaster by company and supplier
     */
    List<PurchaseMaster> findByCompanyRefIdAndSupplierRefId(Integer companyRefId, Integer supplierRefId);

    /**
     * Find PurchaseMaster by sale type
     */
    List<PurchaseMaster> findByCompanyRefIdAndSaleType(Integer companyRefId, String saleType);

    /**
     * Find PurchaseMaster by invoice number
     */
    Optional<PurchaseMaster> findByCompanyRefIdAndInvoiceNo(Integer companyRefId, String invoiceNo);

    /**
     * Find PurchaseMaster by CNumber
     */
    Optional<PurchaseMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find PurchaseMaster by date range
     */
    List<PurchaseMaster> findByCompanyRefIdAndSaleDateBetween(Integer companyRefId, LocalDate startDate, LocalDate endDate);

    /**
     * Find PurchaseMaster by employee
     */
    List<PurchaseMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);

    /**
     * Check if CNumber exists
     */
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find PurchaseMaster by purchase order reference
     */
    Optional<PurchaseMaster> findByPurchaseOrderMasterRefId(Integer purchaseOrderMasterRefId);

    /**
     * Count purchases by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active purchases by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}

