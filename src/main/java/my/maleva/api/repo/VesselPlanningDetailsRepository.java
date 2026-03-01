package my.maleva.api.repo;

import my.maleva.api.model.VesselPlanningDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VesselPlanningDetailsRepository - Repository for VesselPlanningDetails
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface VesselPlanningDetailsRepository extends JpaRepository<VesselPlanningDetails, Integer> {

    /**
     * Find all VesselPlanningDetails records by vessel planning master ID
     */
    List<VesselPlanningDetails> findByVesselPlanningMasterRefId(Integer vesselPlanningMasterRefId);

    /**
     * Find all VesselPlanningDetails records by sale order master ID
     */
    List<VesselPlanningDetails> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Count VesselPlanningDetails records by vessel planning master ID
     */
    long countByVesselPlanningMasterRefId(Integer vesselPlanningMasterRefId);

    /**
     * Delete all VesselPlanningDetails by vessel planning master ID
     */
    void deleteByVesselPlanningMasterRefId(Integer vesselPlanningMasterRefId);
}

