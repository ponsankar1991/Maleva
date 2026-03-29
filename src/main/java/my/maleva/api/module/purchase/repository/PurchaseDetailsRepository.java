package my.maleva.api.module.purchase.repository;

import my.maleva.api.module.purchase.entity.PurchaseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseDetailsRepository extends JpaRepository<PurchaseDetails, Integer> {

    /**
     * Find all PurchaseDetails by purchase master reference
     */
    List<PurchaseDetails> findByPurchaseMasterRefId(Integer purchaseMasterRefId);

    /**
     * Find PurchaseDetails by product master reference
     */
    List<PurchaseDetails> findByProductMasterRefId(Integer productMasterRefId);

    /**
     * Find PurchaseDetails by both purchase master and product master
     */
    List<PurchaseDetails> findByPurchaseMasterRefIdAndProductMasterRefId(Integer purchaseMasterRefId, Integer productMasterRefId);

    /**
     * Count details by purchase master
     */
    long countByPurchaseMasterRefId(Integer purchaseMasterRefId);

    /**
     * Delete details by purchase master (cascade handled in entity)
     */
    void deleteByPurchaseMasterRefId(Integer purchaseMasterRefId);
}

