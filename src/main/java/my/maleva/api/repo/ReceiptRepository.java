package my.maleva.api.repo;

import my.maleva.api.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ReceiptRepository
 * Spring Data JPA Repository for Receipt entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {

    /**
     * Find all Receipt records by company ID
     */
    List<Receipt> findByCompanyRefId(Integer companyRefId);

    /**
     * Find Receipt by customer reference
     */
    List<Receipt> findByCompanyRefIdAndCustomerRefId(Integer companyRefId, Integer customerRefId);

    /**
     * Find Receipt by bank reference
     */
    List<Receipt> findByCompanyRefIdAndBankRefId(Integer companyRefId, Integer bankRefId);

    /**
     * Find Receipt by CNumber
     */
    Optional<Receipt> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find Receipt by date range
     */
    List<Receipt> findByCompanyRefIdAndReceiptDateBetween(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find Receipt by reference number
     */
    Optional<Receipt> findByCompanyRefIdAndRefNumber(Integer companyRefId, String refNumber);

    /**
     * Find Receipt by CNumberDisplay
     */
    Optional<Receipt> findByCNumberDisplay(String cNumberDisplay);

    /**
     * Find Receipt by PV Status
     */
    List<Receipt> findByCompanyRefIdAndPvStatus(Integer companyRefId, Integer pvStatus);

    /**
     * Check if CNumber exists
     */
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Count Receipt by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count Receipt by PV Status
     */
    long countByCompanyRefIdAndPvStatus(Integer companyRefId, Integer pvStatus);
}

