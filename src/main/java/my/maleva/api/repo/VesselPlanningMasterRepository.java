package my.maleva.api.repo;

import my.maleva.api.model.VesselPlanningMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * VesselPlanningMasterRepository - Repository for VesselPlanningMaster
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface VesselPlanningMasterRepository extends JpaRepository<VesselPlanningMaster, Integer> {

    /**
     * Find all VesselPlanningMaster records by company ID
     */
    List<VesselPlanningMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find all active VesselPlanningMaster records by company
     */
    List<VesselPlanningMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find VesselPlanningMaster by C Number and Company
     */
    Optional<VesselPlanningMaster> findByCNumberAndCompanyRefId(Integer cNumber, Integer companyRefId);

    /**
     * Find all VesselPlanningMaster records by user ID
     */
    List<VesselPlanningMaster> findByUserRefId(Integer userRefId);

    /**
     * Find all VesselPlanningMaster records by employee ID
     */
    List<VesselPlanningMaster> findByEmployeeRefId(Integer employeeRefId);

    /**
     * Find VesselPlanningMaster records by date range
     */
    List<VesselPlanningMaster> findByFDateGreaterThanEqualAndTDateLessThanEqual(
        LocalDate startDate, LocalDate endDate);

    /**
     * Find VesselPlanningMaster records by company and date range
     */
    List<VesselPlanningMaster> findByCompanyRefIdAndFDateGreaterThanEqualAndTDateLessThanEqual(
        Integer companyRefId, LocalDate startDate, LocalDate endDate);

    /**
     * Count VesselPlanningMaster records by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active VesselPlanningMaster records by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);
}

