package my.maleva.api.repo;

import my.maleva.api.model.SaleDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleDetailsRepository
 * Spring Data JPA Repository for SaleDetails entity
 */
@Repository
public interface SaleDetailsRepository extends JpaRepository<SaleDetails, Integer> {

    List<SaleDetails> findBySaleMasterRefId(Integer saleMasterRefId);
    List<SaleDetails> findByItemMasterRefId(Integer itemMasterRefId);
    long countBySaleMasterRefId(Integer saleMasterRefId);
}

