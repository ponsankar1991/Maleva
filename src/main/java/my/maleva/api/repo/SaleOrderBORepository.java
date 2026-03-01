package my.maleva.api.repo;

import my.maleva.api.model.SaleOrderBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleOrderBORepository - Repository for SaleOrderBO
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SaleOrderBORepository extends JpaRepository<SaleOrderBO, Integer> {

    /**
     * Find all SaleOrderBO records by SaleOrderMasterRefId
     */
    List<SaleOrderBO> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Find all SaleOrderBO records by BOTypeId
     */
    List<SaleOrderBO> findByBoTypeId(Integer boTypeId);

    /**
     * Find all SaleOrderBO records by Status
     */
    List<SaleOrderBO> findByStatus(Integer status);

    /**
     * Count SaleOrderBO records by SaleOrderMasterRefId
     */
    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Delete all SaleOrderBO records by SaleOrderMasterRefId
     */
    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

