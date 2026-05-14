package my.maleva.api.module.purchase.service;

import my.maleva.api.module.purchase.dto.*;
import java.math.BigDecimal;
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

    /**
     * Calculate total payment amount for a purchase order
     * @param companyId Company identifier
     * @param purchaseId Purchase order identifier
     * @return Total payment amount as BigDecimal
     */
    BigDecimal checkEditAmount(Integer companyId, Integer purchaseId);

    /**
     * Soft delete PurchaseMaster record (set Active=2)
     * Equivalent to .NET DeletePurchaseMaster method
     * @param id Purchase Master ID
     * @param companyId Company ID for validation
     * @return true if deleted successfully, false if not found
     */
    boolean softDelete(Integer id, Integer companyId);

    /**
     * Get the next PurchaseMaster number in format "PM" + 9-digit padded sequence
     * Equivalent to .NET MaxPurchaseMasterNo method
     * @param companyId Company identifier
     * @return Formatted purchase master number (e.g., "PM000000001")
     */
    String getMaxPurchaseMasterNo(Integer companyId);

    /**
     * Get distinct descriptions from PurchaseMaster for a company
     * Equivalent to .NET SelectDescription method
     * @param companyId Company identifier
     * @return List of distinct descriptions
     */
    List<String> getDistinctDescriptions(Integer companyId);

    /**
     * Insert PurchaseMaster records using stored procedure
     * Equivalent to .NET InsertPurchaseMaster method
     * @param purchaseMasters List of PurchaseMaster DTOs
     * @param companyId Company identifier
     * @return InsertPurchaseMasterResponseDto with result
     */
    InsertPurchaseMasterResponseDto insertPurchaseMaster(List<PurchaseMasterDto> purchaseMasters, Integer companyId);

    /**
     * Get spare parts report view with multiple filters
     * Equivalent to .NET SelectSparePartsView method
     * Performs complex JOIN with Supplier, Employee, Truck, Driver, ProductMaster
     * Supports filtering by supplier, employee, driver, truck, product, and date/invoice search
     *
     * @param request SelectSparePartsViewRequestDto containing all filter parameters
     * @return SelectSparePartsViewResponseDto with report data or error message
     */
    SelectSparePartsViewResponseDto selectSparePartsView(SelectSparePartsViewRequestDto request);

    /**
     * Get purchase master records with multiple filters and combined view
     * Equivalent to .NET SelectPurchaseMaster method
     * Retrieves both master and detail records in a single combined view
     *
     * Supports flexible filtering:
     * <ul>
     *   <li>Supplier filter: supplierId > 0</li>
     *   <li>Employee filter: employeeId > 0</li>
     *   <li>Driver filter: driverId > 0</li>
     *   <li>Truck filter: truckId > 0</li>
     *   <li>Product filter: productId > 0</li>
     *   <li>Search filter: By CNumberDisplay or InvoiceNo (overrides date filter)</li>
     *   <li>Date filter: By InvoiceDate (invoiceCheck=1) or SaleDate (invoiceCheck=0)</li>
     * </ul>
     *
     * @param request SelectPurchaseMasterRequestDto containing all filter parameters
     * @return SelectPurchaseMasterResponseDto with combined view data or error message
     *
     * @throws IllegalArgumentException if companyId is null or invalid
     */
    SelectPurchaseMasterResponseDto selectPurchaseMaster(SelectPurchaseMasterRequestDto request);

    /**
     * Get full PurchaseMaster record with all details for editing
     * Equivalent to .NET EditPurchaseMaster method
     *
     * Retrieves a single PurchaseMaster record with all its details and related data:
     * - Master record fields (invoice, dates, amounts, etc.)
     * - All PurchaseDetails items with product information
     * - Product master data and UOM information
     *
     * When purchaseMasterNo is provided and id is 0/null, resolves ID via CNumber lookup
     *
     * @param request EditPurchaseMasterRequestDto containing id/purchaseMasterNo and companyId
     * @return EditPurchaseMasterResponseDto with full master + details or error message
     *
     * @throws IllegalArgumentException if companyId is null or invalid
     * @throws ResourceNotFoundException if record not found with given id/masterNo
     */
    EditPurchaseMasterResponseDto editPurchaseMaster(EditPurchaseMasterRequestDto request);
}
