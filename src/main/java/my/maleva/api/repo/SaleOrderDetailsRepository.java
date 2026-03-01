package my.maleva.api.repo;

import my.maleva.api.model.SaleOrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleOrderDetailsRepository - Repository for SaleOrderDetails
 */
@Repository
public interface SaleOrderDetailsRepository extends JpaRepository<SaleOrderDetails, Integer> {

    List<SaleOrderDetails> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
    List<SaleOrderDetails> findByItemMasterRefId(Integer itemMasterRefId);
    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

