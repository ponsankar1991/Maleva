package my.maleva.api.module.planning.repository;

import my.maleva.api.module.planning.entity.PlanningMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanningMasterRepository extends JpaRepository<PlanningMaster, Integer> {

    /**
     * Find all planning records by company ID
     */
    List<PlanningMaster> findByCompanyRefIdAndActiveNot(Integer companyRefId, Integer active);

    /**
     * Find planning records by company and employee
     */
    List<PlanningMaster> findByCompanyRefIdAndEmployeeRefIdAndActiveNot(
            Integer companyRefId, Integer employeeRefId, Integer active);

    /**
     * Find planning records by date range
     */
    @Query("SELECT p FROM PlanningMaster p WHERE p.companyRefId = :companyId " +
            "AND p.saleDate >= :fromDate AND p.saleDate <= :toDate " +
            "AND p.active != 2")
    List<PlanningMaster> findByCompanyAndDateRange(
            @Param("companyId") Integer companyId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * Find planning record by CNumber
     *
     * NOTE: use explicit @Query with the correct Java attribute name `cNumber` (lowercase 'c')
     * because derived query generation can produce a JPQL path using `CNumber` which
     * doesn't match the entity attribute name and causes UnknownPathException.
     */
    @Query("SELECT p FROM PlanningMaster p WHERE p.companyRefId = :companyRefId AND p.cNumber = :cNumber")
    Optional<PlanningMaster> findByCompanyRefIdAndCNumber(@Param("companyRefId") Integer companyRefId, @Param("cNumber") Integer cNumber);

    /**
     * Find planning record by CNumberDisplay
     */
    Optional<PlanningMaster> findByCompanyRefIdAndCNumberDisplay(Integer companyRefId, String cNumberDisplay);

    /**
     * Search planning records by search criteria
     */
    @Query("SELECT p FROM PlanningMaster p WHERE p.companyRefId = :companyId " +
            "AND (p.search LIKE %:search% OR p.remarks LIKE %:search%) " +
            "AND p.active != 2")
    List<PlanningMaster> searchByCompanyAndKeyword(
            @Param("companyId") Integer companyId,
            @Param("search") String search);

    /**
     * Find planning masters for SelectPLANING with filters
     */
    @Query("SELECT p FROM PlanningMaster p WHERE p.companyRefId = :companyId AND p.active = 1 " +
            "AND (:employeeId IS NULL OR p.employeeRefId = :employeeId) " +
            "AND (:fromDate IS NULL OR :toDate IS NULL OR p.saleDate BETWEEN :fromDate AND :toDate)")
    List<PlanningMaster> findForSelectPlanning(
            @Param("companyId") Integer companyId,
            @Param("employeeId") Integer employeeId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * Update CNumberDisplay and CNumber for a planning record.
     * Used during sequence number generation (SP_PLANINGMaster logic).
     */
    @Modifying
    @Query("UPDATE PlanningMaster p SET p.cNumberDisplay = :cNumberDisplay, p.cNumber = :cNumber WHERE p.id = :id")
    void updateCNumberDisplay(@Param("id") Integer id, @Param("cNumberDisplay") String cNumberDisplay, @Param("cNumber") Integer cNumber);
}
