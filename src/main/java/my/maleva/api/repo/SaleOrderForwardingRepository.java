package my.maleva.api.repo;

import my.maleva.api.model.SaleOrderForwarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleOrderForwardingRepository - Repository for SaleOrderForwarding
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SaleOrderForwardingRepository extends JpaRepository<SaleOrderForwarding, Integer> {

    /**
     * Find all SaleOrderForwarding records by SaleOrderMasterRefId
     */
    List<SaleOrderForwarding> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Count SaleOrderForwarding records by SaleOrderMasterRefId
     */
    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Delete all SaleOrderForwarding records by SaleOrderMasterRefId
     */
    void deleteAllBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

