package my.maleva.api.module.purchase.repository;

import my.maleva.api.module.purchase.entity.PurchaseOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * PurchaseOrderDetailsRepository
 * Spring Data JPA Repository for PurchaseOrderDetails entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface PurchaseOrderDetailsRepository extends JpaRepository<PurchaseOrderDetails, Integer> {

    /**
     * Find all PurchaseOrderDetails by PurchaseOrderMaster ID
     */
    List<PurchaseOrderDetails> findByPurchaseOrderMasterRefId(Integer purchaseOrderMasterRefId);

    /**
     * Find PurchaseOrderDetails by product
     */
    List<PurchaseOrderDetails> findByProductMasterRefId(Integer productMasterRefId);

    /**
     * Count details for a PurchaseOrderMaster
     */
    long countByPurchaseOrderMasterRefId(Integer purchaseOrderMasterRefId);

    /**
     * Delete all details for a PurchaseOrderMaster
     */
    void deleteByPurchaseOrderMasterRefId(Integer purchaseOrderMasterRefId);
}

