package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.saleorder.entity.SaleOrderPickup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleOrderPickupRepository - Repository for SaleOrderPickup
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SaleOrderPickupRepository extends JpaRepository<SaleOrderPickup, Integer> {

    /**
     * Find all SaleOrderPickup records by SaleOrderMasterRefId
     */
    List<SaleOrderPickup> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Count SaleOrderPickup records by SaleOrderMasterRefId
     */
    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Delete all SaleOrderPickup records by SaleOrderMasterRefId
     */
    void deleteAllBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

