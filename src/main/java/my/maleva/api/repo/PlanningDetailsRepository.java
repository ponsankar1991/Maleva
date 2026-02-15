package my.maleva.api.repo;

import my.maleva.api.model.PlanningDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanningDetailsRepository extends JpaRepository<PlanningDetails, Integer> {

    /**
     * Find all details by planning master reference ID
     */
    List<PlanningDetails> findByPlanningMasterRefId(Integer planningMasterRefId);

    /**
     * Find details by sale order master reference ID
     */
    List<PlanningDetails> findBySaleOrderMasterRefId(Integer saleOrderMasterRefId);

    /**
     * Find details by truck reference ID
     */
    List<PlanningDetails> findByTruckRefId(Integer truckRefId);

    /**
     * Find details by planning master and sorted
     */
    @Query("SELECT p FROM PlanningDetails p WHERE p.planningMasterRefId = :masterRefId " +
            "ORDER BY p.sortBy ASC")
    List<PlanningDetails> findByPlanningMasterRefIdSorted(@Param("masterRefId") Integer masterRefId);

    /**
     * Delete all details by planning master reference ID
     */
    void deleteByPlanningMasterRefId(Integer planningMasterRefId);
}

