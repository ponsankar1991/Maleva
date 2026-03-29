package my.maleva.api.module.paymentrecept.repository;

import my.maleva.api.module.paymentrecept.entity.ReceiptDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * ReceiptDetailsRepository
 * Spring Data JPA Repository for ReceiptDetails entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface ReceiptDetailsRepository extends JpaRepository<ReceiptDetails, Integer> {

    /**
     * Find all ReceiptDetails by Receipt ID
     */
    List<ReceiptDetails> findByReceiptRefId(Integer receiptRefId);

    /**
     * Find ReceiptDetails by sale master reference
     */
    List<ReceiptDetails> findBySaleMasterRefId(Integer saleMasterRefId);

    /**
     * Find ReceiptDetails by customer open reference
     */
    List<ReceiptDetails> findByCustomerOpenRefId(Integer customerOpenRefId);

    /**
     * Count details for a Receipt
     */
    long countByReceiptRefId(Integer receiptRefId);

    /**
     * Delete all details for a Receipt
     */
    void deleteByReceiptRefId(Integer receiptRefId);
}

