package my.maleva.api.repo;

import my.maleva.api.model.SaleCreditDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleCreditDetailsRepository
 * Spring Data JPA Repository for SaleCreditDetails entity
 */
@Repository
public interface SaleCreditDetailsRepository extends JpaRepository<SaleCreditDetails, Integer> {

    /**
     * Find all SaleCreditDetails by Sale Credit Master Reference ID
     */
    List<SaleCreditDetails> findBySaleCreditMasterRefId(Integer saleCreditMasterRefId);

    /**
     * Find SaleCreditDetails by item ID
     */
    List<SaleCreditDetails> findByItemMasterRefId(Integer itemMasterRefId);

    /**
     * Count details by Sale Credit Master Reference ID
     */
    long countBySaleCreditMasterRefId(Integer saleCreditMasterRefId);
}

