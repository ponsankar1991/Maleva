package my.maleva.api.module.saleorder.repository;

import my.maleva.api.module.saleorder.entity.SaleOrderBONotRequired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleOrderBONotRequiredRepository - Repository for SaleOrderBONotRequired
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SaleOrderBONotRequiredRepository extends JpaRepository<SaleOrderBONotRequired, Integer> {

    /**
     * Find all SaleOrderBONotRequired records by SaleOrderMasterRefId
     */
    List<SaleOrderBONotRequired> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Find all SaleOrderBONotRequired records by BOTypeId
     */
    List<SaleOrderBONotRequired> findByBoTypeId(Integer boTypeId);

    /**
     * Count SaleOrderBONotRequired records by SaleOrderMasterRefId
     */
    long countBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Delete all SaleOrderBONotRequired records by SaleOrderMasterRefId
     */
    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

