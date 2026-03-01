package my.maleva.api.repo;

import my.maleva.api.model.SaleOrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleOrderDeliveryRepository - Repository for SaleOrderDelivery
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SaleOrderDeliveryRepository extends JpaRepository<SaleOrderDelivery, Integer> {

    /**
     * Find all SaleOrderDelivery records by SaleOrderMasterRefId
     */
    List<SaleOrderDelivery> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Count SaleOrderDelivery records by SaleOrderMasterRefId
     */
    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Delete all SaleOrderDelivery records by SaleOrderMasterRefId
     */
    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

