package my.maleva.api.module.paymentrecept.service;

import my.maleva.api.module.paymentrecept.dto.ReceiptDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ReceiptService
 * Business logic interface for Receipt operations
 */
public interface ReceiptService {

    /**
     * Get all Receipt records by company ID
     */
    List<ReceiptDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get Receipt by ID
     */
    Optional<ReceiptDto> getById(Integer id);

    /**
     * Create new Receipt record
     */
    ReceiptDto create(ReceiptDto dto);

    /**
     * Update Receipt record
     */
    ReceiptDto update(Integer id, ReceiptDto dto);

    /**
     * Delete Receipt record
     */
    boolean delete(Integer id);

    /**
     * Get Receipt by customer reference
     */
    List<ReceiptDto> getByCustomer(Integer companyRefId, Integer customerRefId);

    /**
     * Get Receipt by bank reference
     */
    List<ReceiptDto> getByBank(Integer companyRefId, Integer bankRefId);

    /**
     * Get Receipt by CNumber
     */
    Optional<ReceiptDto> getByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Get Receipt by date range
     */
    List<ReceiptDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get Receipt by reference number
     */
    Optional<ReceiptDto> getByRefNumber(Integer companyRefId, String refNumber);

    /**
     * Get Receipt by CNumberDisplay
     */
    Optional<ReceiptDto> getByCNumberDisplay(String cNumberDisplay);

    /**
     * Get Receipt by PV Status
     */
    List<ReceiptDto> getByPvStatus(Integer companyRefId, Integer pvStatus);

    /**
     * Check if CNumber exists
     */
    boolean existsByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Count Receipt by company
     */
    long countByCompanyId(Integer companyRefId);

    /**
     * Count Receipt by PV Status
     */
    long countByPvStatus(Integer companyRefId, Integer pvStatus);

    /**
     * Generate CNumberDisplay
     */
    String generateCNumberDisplay(Integer cNumber);

    /**
     * Change receipt status
     */
    ReceiptDto changeStatus(Integer id, Integer pvStatus);

    /**
     * Get maximum receipt number
     */
    String getMaxReceiptNo(Integer companyRefId, String billType);

    /**
     * Get customer balance (from CustomerBalance or CustomerBalance_Single)
     */
    List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto> selectCustomerBalance(my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest request);

    /**
     * Select customer bills for receipt entry (from RT_CustomerBills)
     */
    List<my.maleva.api.module.paymentrecept.dto.ReceiptBillDto> selectCustomerBills(my.maleva.api.module.paymentrecept.dto.ReceiptViewBillRequest request);

    /**
     * Insert or update receipt with line items (migrated from SP_Receipt)
     */
    my.maleva.api.module.paymentrecept.dto.ReceiptSaveResponseDto insertReceipt(
            List<my.maleva.api.module.paymentrecept.dto.ReceiptSaveRequest> requestList,
            Integer companyId);

    /** The RECEIPT ENTRY VIEW grid (legacy SelectReceipt). */
    my.maleva.api.module.paymentrecept.dto.ReceiptViewDto search(
            my.maleva.api.module.paymentrecept.dto.ReceiptSearchRequest request);

    /**
     * A saved receipt loaded back into the entry screen (legacy EditReceipt),
     * by id or by receipt number; empty when it does not exist for the company.
     */
    Optional<my.maleva.api.module.paymentrecept.dto.ReceiptEditDto> edit(
            Integer companyId, Integer id, Integer receiptNumber);

    /**
     * Removes a receipt and its lines (legacy DeleteReceipt), refusing one that
     * is already in QNE. Returns the message for the screen; throws
     * InvalidRequestException with the reason when it cannot be deleted.
     */
    String deleteReceipt(Integer id, Integer companyId);

}

