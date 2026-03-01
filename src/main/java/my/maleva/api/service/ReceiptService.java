package my.maleva.api.service;

import my.maleva.api.dto.ReceiptDto;
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
}

