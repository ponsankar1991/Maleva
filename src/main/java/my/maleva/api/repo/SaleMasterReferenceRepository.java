package my.maleva.api.repo;

import my.maleva.api.model.SaleMasterReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * SaleMasterReferenceRepository - Repository for SaleMasterReference
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SaleMasterReferenceRepository extends JpaRepository<SaleMasterReference, Integer> {

    /**
     * Find all SaleMasterReference records by SaleMasterRefId
     */
    List<SaleMasterReference> findBySaleMasterRefId(Integer saleMasterRefId);

    /**
     * Find all SaleMasterReference records by SaleOrderMasterRefId
     */
    List<SaleMasterReference> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Count SaleMasterReference records by SaleMasterRefId
     */
    long countBySaleMasterRefId(Integer saleMasterRefId);

    /**
     * Delete all SaleMasterReference records by SaleOrderMasterRefId
     */
    void deleteBySaleOrderMasterRefId(Integer saleOrderMasterRefId);
}

