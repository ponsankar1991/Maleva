package my.maleva.api.service;

import my.maleva.api.dto.PurchaseDetailsDto;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for PurchaseDetails operations
 * Contains business logic for purchase order line items
 */
public interface PurchaseDetailsService {

    /**
     * Get all PurchaseDetails by purchase master
     */
    List<PurchaseDetailsDto> getByPurchaseMaster(Integer purchaseMasterRefId);

    /**
     * Get PurchaseDetails by ID
     */
    Optional<PurchaseDetailsDto> getById(Integer id);

    /**
     * Create new PurchaseDetails record
     */
    PurchaseDetailsDto create(PurchaseDetailsDto dto);

    /**
     * Create multiple PurchaseDetails records
     */
    List<PurchaseDetailsDto> createBatch(List<PurchaseDetailsDto> dtos);

    /**
     * Update existing PurchaseDetails record
     */
    PurchaseDetailsDto update(Integer id, PurchaseDetailsDto dto);

    /**
     * Delete PurchaseDetails record
     */
    boolean delete(Integer id);

    /**
     * Delete all details by purchase master
     */
    boolean deleteByPurchaseMaster(Integer purchaseMasterRefId);

    /**
     * Get PurchaseDetails by product master
     */
    List<PurchaseDetailsDto> getByProduct(Integer productMasterRefId);

    /**
     * Count details by purchase master
     */
    long countByPurchaseMaster(Integer purchaseMasterRefId);

    /**
     * Calculate total amount for purchase details
     */
    Double calculateTotalAmount(Integer purchaseMasterRefId);

    /**
     * Calculate total tax for purchase details
     */
    Double calculateTotalTax(Integer purchaseMasterRefId);

    /**
     * Calculate total discount for purchase details
     */
    Double calculateTotalDiscount(Integer purchaseMasterRefId);
}

