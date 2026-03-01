package my.maleva.api.service;

import my.maleva.api.dto.PurchaseOrderDetailsDto;
import java.util.List;
import java.util.Optional;

/**
 * PurchaseOrderDetailsService
 * Business logic interface for PurchaseOrderDetails operations
 */
public interface PurchaseOrderDetailsService {

    /**
     * Get all PurchaseOrderDetails by PurchaseOrderMaster ID
     */
    List<PurchaseOrderDetailsDto> getByPurchaseOrderMasterId(Integer purchaseOrderMasterRefId);

    /**
     * Get PurchaseOrderDetails by ID
     */
    Optional<PurchaseOrderDetailsDto> getById(Integer id);

    /**
     * Create new PurchaseOrderDetails record
     */
    PurchaseOrderDetailsDto create(PurchaseOrderDetailsDto dto);

    /**
     * Update PurchaseOrderDetails record
     */
    PurchaseOrderDetailsDto update(Integer id, PurchaseOrderDetailsDto dto);

    /**
     * Delete PurchaseOrderDetails record
     */
    boolean delete(Integer id);

    /**
     * Get PurchaseOrderDetails by product
     */
    List<PurchaseOrderDetailsDto> getByProductMasterId(Integer productMasterRefId);

    /**
     * Count PurchaseOrderDetails for a PurchaseOrderMaster
     */
    long countByPurchaseOrderMasterId(Integer purchaseOrderMasterRefId);

    /**
     * Delete all PurchaseOrderDetails for a PurchaseOrderMaster
     */
    void deleteByPurchaseOrderMasterId(Integer purchaseOrderMasterRefId);
}

