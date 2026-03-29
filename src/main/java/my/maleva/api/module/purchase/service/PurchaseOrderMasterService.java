package my.maleva.api.module.purchase.service;

import my.maleva.api.module.purchase.dto.PurchaseOrderMasterDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PurchaseOrderMasterService
 * Business logic interface for PurchaseOrderMaster operations
 */
public interface PurchaseOrderMasterService {

    /**
     * Get all PurchaseOrderMaster records by company ID
     */
    List<PurchaseOrderMasterDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get active PurchaseOrderMaster records by company ID
     */
    List<PurchaseOrderMasterDto> getActiveByCompanyId(Integer companyRefId);

    /**
     * Get PurchaseOrderMaster by ID
     */
    Optional<PurchaseOrderMasterDto> getById(Integer id);

    /**
     * Create new PurchaseOrderMaster record
     */
    PurchaseOrderMasterDto create(PurchaseOrderMasterDto dto);

    /**
     * Update PurchaseOrderMaster record
     */
    PurchaseOrderMasterDto update(Integer id, PurchaseOrderMasterDto dto);

    /**
     * Delete PurchaseOrderMaster record
     */
    boolean delete(Integer id);

    /**
     * Get PurchaseOrderMaster by invoice number
     */
    Optional<PurchaseOrderMasterDto> getByInvoiceNo(Integer companyRefId, String invoiceNo);

    /**
     * Get PurchaseOrderMaster by supplier
     */
    List<PurchaseOrderMasterDto> getBySupplier(Integer companyRefId, Integer supplierRefId);

    /**
     * Get PurchaseOrderMaster by sale type
     */
    List<PurchaseOrderMasterDto> getBySaleType(Integer companyRefId, String saleType);

    /**
     * Get PurchaseOrderMaster by date range
     */
    List<PurchaseOrderMasterDto> getByDateRange(Integer companyRefId, LocalDate startDate, LocalDate endDate);

    /**
     * Get PurchaseOrderMaster by employee
     */
    List<PurchaseOrderMasterDto> getByEmployee(Integer companyRefId, Integer employeeRefId);

    /**
     * Get PurchaseOrderMaster by CNumber
     */
    Optional<PurchaseOrderMasterDto> getByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Check if CNumber exists
     */
    boolean existsByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Count PurchaseOrderMaster by company
     */
    long countByCompanyId(Integer companyRefId);

    /**
     * Count active PurchaseOrderMaster by company
     */
    long countActiveByCompanyId(Integer companyRefId);

    /**
     * Activate PurchaseOrderMaster
     */
    PurchaseOrderMasterDto activate(Integer id);

    /**
     * Deactivate PurchaseOrderMaster
     */
    PurchaseOrderMasterDto deactivate(Integer id);

    /**
     * Generate CNumberDisplay
     */
    String generateCNumberDisplay(Integer cNumber);
}

