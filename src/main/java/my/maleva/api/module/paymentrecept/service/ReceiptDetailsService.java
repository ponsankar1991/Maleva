package my.maleva.api.module.paymentrecept.service;

import my.maleva.api.module.paymentrecept.dto.ReceiptDetailsDto;
import java.util.List;
import java.util.Optional;

/**
 * ReceiptDetailsService
 * Business logic interface for ReceiptDetails operations
 */
public interface ReceiptDetailsService {

    /**
     * Get all ReceiptDetails by Receipt ID
     */
    List<ReceiptDetailsDto> getByReceiptId(Integer receiptRefId);

    /**
     * Get ReceiptDetails by ID
     */
    Optional<ReceiptDetailsDto> getById(Integer id);

    /**
     * Create new ReceiptDetails record
     */
    ReceiptDetailsDto create(ReceiptDetailsDto dto);

    /**
     * Update ReceiptDetails record
     */
    ReceiptDetailsDto update(Integer id, ReceiptDetailsDto dto);

    /**
     * Delete ReceiptDetails record
     */
    boolean delete(Integer id);

    /**
     * Get ReceiptDetails by sale master reference
     */
    List<ReceiptDetailsDto> getBySaleMasterId(Integer saleMasterRefId);

    /**
     * Get ReceiptDetails by customer open reference
     */
    List<ReceiptDetailsDto> getByCustomerOpenId(Integer customerOpenRefId);

    /**
     * Count ReceiptDetails for a Receipt
     */
    long countByReceiptId(Integer receiptRefId);

    /**
     * Delete all ReceiptDetails for a Receipt
     */
    void deleteByReceiptId(Integer receiptRefId);
}

