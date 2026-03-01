package my.maleva.api.service;

import my.maleva.api.dto.PurchaseMasterDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for PurchaseMaster operations
 * Contains business logic for purchase order management
 */
public interface PurchaseMasterService {

    /**
     * Get all PurchaseMaster records by company ID
     */
    List<PurchaseMasterDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get active PurchaseMaster records by company ID
     */
    List<PurchaseMasterDto> getActiveByCompanyId(Integer companyRefId);

    /**
     * Get PurchaseMaster by ID
     */
    Optional<PurchaseMasterDto> getById(Integer id);

    /**
     * Create new PurchaseMaster record
     * Implements SP_PurchaseMaster stored procedure logic
     */
    PurchaseMasterDto create(PurchaseMasterDto dto);

    /**
     * Update existing PurchaseMaster record
     * Implements SP_PurchaseMaster stored procedure logic
     */
    PurchaseMasterDto update(Integer id, PurchaseMasterDto dto);

    /**
     * Delete PurchaseMaster record
     */
    boolean delete(Integer id);

    /**
     * Get PurchaseMaster by invoice number
     */
    Optional<PurchaseMasterDto> getByInvoiceNo(Integer companyRefId, String invoiceNo);

    /**
     * Get PurchaseMaster by supplier
     */
    List<PurchaseMasterDto> getBySupplier(Integer companyRefId, Integer supplierRefId);

    /**
     * Get PurchaseMaster by sale type
     */
    List<PurchaseMasterDto> getBySaleType(Integer companyRefId, String saleType);

    /**
     * Get PurchaseMaster by date range
     */
    List<PurchaseMasterDto> getByDateRange(Integer companyRefId, LocalDate startDate, LocalDate endDate);

    /**
     * Get PurchaseMaster by employee
     */
    List<PurchaseMasterDto> getByEmployee(Integer companyRefId, Integer employeeRefId);

    /**
     * Check if CNumber exists
     */
    boolean existsByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Get PurchaseMaster by CNumber
     */
    Optional<PurchaseMasterDto> getByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Get PurchaseMaster by purchase order reference
     */
    Optional<PurchaseMasterDto> getByPurchaseOrderRef(Integer purchaseOrderMasterRefId);

    /**
     * Count purchases by company
     */
    long countByCompanyId(Integer companyRefId);

    /**
     * Count active purchases by company
     */
    long countActiveByCompanyId(Integer companyRefId);

    /**
     * Activate PurchaseMaster record
     */
    PurchaseMasterDto activate(Integer id);

    /**
     * Deactivate PurchaseMaster record
     */
    PurchaseMasterDto deactivate(Integer id);

    /**
     * Get next CNumber for company
     */
    Integer getNextCNumber(Integer companyRefId);

    /**
     * Generate CNumberDisplay
     */
    String generateCNumberDisplay(Integer cNumber);
}

