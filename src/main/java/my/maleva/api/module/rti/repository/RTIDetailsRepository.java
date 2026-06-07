package my.maleva.api.module.rti.repository;

import my.maleva.api.module.rti.entity.RTIDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * RTIDetailsRepository
 * Spring Data JPA Repository for RTIDetails entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface RTIDetailsRepository extends JpaRepository<RTIDetails, Integer> {

    /**
     * Find all RTIDetails by RTIMaster ID
     */
    List<RTIDetails> findByRtiMasterRefId(Integer rtiMasterRefId);

    /**
     * Find RTIDetails by sale order master
     */
    List<RTIDetails> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Count details for an RTIMaster
     */
    long countByRtiMasterRefId(Integer rtiMasterRefId);

    /**
     * Delete all details for an RTIMaster
     */
    void deleteByRtiMasterRefId(Integer rtiMasterRefId);

    /**
     * Fetch RTIDetails with their associated SaleOrderMaster and Customer names
     * to prevent N+1 queries during revise logic.
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT d, sm, c.customerName FROM RTIDetails d " +
        "LEFT JOIN SaleOrderMaster sm ON sm.id = d.saleOrderMasterRefId " +
        "LEFT JOIN my.maleva.api.module.customer.entity.Customer c ON c.id = sm.customerRefId " +
        "WHERE d.rtiMasterRefId = :rtiMasterId"
    )
    List<Object[]> findDetailsWithEnrichment(@org.springframework.data.repository.query.Param("rtiMasterId") Integer rtiMasterId);
}

