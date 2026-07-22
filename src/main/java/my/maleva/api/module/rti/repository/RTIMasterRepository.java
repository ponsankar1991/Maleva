package my.maleva.api.module.rti.repository;

import my.maleva.api.module.rti.entity.RTIMaster;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RTIMasterRepository
 * Spring Data JPA Repository for RTIMaster entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface RTIMasterRepository extends JpaRepository<RTIMaster, Integer> {

    /**
     * Find all RTIMaster records by company ID
     */
    List<RTIMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active RTIMaster records by company ID
     */
    List<RTIMaster> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Legacy SelectRTI equivalent when RTI number search is provided.
     * Search intentionally ignores date/driver/truck/employee filters, matching the old .NET flow.
     */
    @Query("""
            SELECT r
            FROM RTIMaster r
            WHERE r.companyRefId = :companyRefId
              AND r.active = 1
              AND r.CNumberDisplay = :search
            ORDER BY r.saleDate ASC, r.id ASC
            """)
    List<RTIMaster> findActiveByCompanyAndRtiNo(
            @Param("companyRefId") Integer companyRefId,
            @Param("search") String search);

    /**
     * Legacy SelectRTI equivalent for date and optional driver/truck/employee filters.
     */
    @Query("""
            SELECT r
            FROM RTIMaster r
            WHERE r.companyRefId = :companyRefId
              AND r.active = 1
              AND (:fromDate IS NULL OR r.saleDate >= :fromDate)
              AND (:toDate IS NULL OR r.saleDate <= :toDate)
              AND (:driverId IS NULL OR :driverId = 0 OR r.driverRefId = :driverId)
              AND (:truckId IS NULL OR :truckId = 0 OR r.truckRefId = :truckId)
              AND (:employeeId IS NULL OR :employeeId = 0 OR r.employeeRefId = :employeeId)
            ORDER BY r.saleDate ASC, r.id ASC
            """)
    List<RTIMaster> findActiveByCompanyWithFilters(
            @Param("companyRefId") Integer companyRefId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("driverId") Integer driverId,
            @Param("truckId") Integer truckId,
            @Param("employeeId") Integer employeeId);


    /**
     * Delete RTIMaster by id
     */
    void deleteById(Integer id);

    /**
     * High performance RTI View Query matching complex SQL requirement
     */
    @Query("SELECT new my.maleva.api.module.rti.dto.RTIViewDto(" +
           "rm.id, rm.CNumberDisplay, rm.CNumber, rm.saleDate, rm.sealBy, rm.breakSealBy, " +
           "sm.cNumberDisplay, em.employeeName, dm.driverName, tm.truckName) " +
           "FROM RTIMaster rm " +
           "JOIN RTIDetails rd ON rm.id = rd.rtiMasterRefId " +
           "JOIN my.maleva.api.module.saleorder.entity.SaleOrderMaster sm ON sm.id = rd.saleOrderMasterRefId " +
           "JOIN my.maleva.api.module.employee.entity.EmployeeMaster em ON em.id = sm.employeeRefId " +
           "JOIN my.maleva.api.module.fleet.entity.DriverMaster dm ON dm.id = rm.driverRefId " +
           "JOIN my.maleva.api.module.fleet.entity.TruckMaster tm ON tm.id = rm.truckRefId " +
           "WHERE (:fromDate IS NULL OR CAST(rm.saleDate AS date) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(rm.saleDate AS date) <= :toDate) " +
           "AND (:employeeId IS NULL OR :employeeId = 0 OR sm.employeeRefId = :employeeId) " +
           "ORDER BY rm.id ASC")
    List<my.maleva.api.module.rti.dto.RTIViewDto> findRtiViewDetails(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            @Param("employeeId") Integer employeeId);

    /**
     * Find RTIMaster by ID
     */

    Optional<RTIMaster> findByCompanyRefIdAndCNumberAndActive(Integer companyRefId, Integer cNumber, Integer active);

    Optional<RTIMaster> findByIdAndActive(Integer id, Integer active);    /**
     * Find RTIMaster by CNumber
     */
    Optional<RTIMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find RTIMaster by employee
     */
    List<RTIMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);

    /**
     * Find RTIMaster by agent
     */
    List<RTIMaster> findByCompanyRefIdAndAgentMasterRefId(Integer companyRefId, Integer agentMasterRefId);

    /**
     * Find RTIMaster by date range
     */
    List<RTIMaster> findByCompanyRefIdAndSaleDateBetween(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find RTIMaster by CNumberDisplay
     */
    Optional<RTIMaster> findByCNumberDisplay(String cNumberDisplay);

    /**
     * Find sleeping RTI records
     */
    List<RTIMaster> findByCompanyRefIdAndSleeping(Integer companyRefId, Integer sleeping);

    /**
     * Find RTIMaster by truck
     */
    List<RTIMaster> findByCompanyRefIdAndTruckRefId(Integer companyRefId, Integer truckRefId);

    /**
     * Check if CNumber exists
     */
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Count RTIMaster by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active RTIMaster by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find the maximum CNumber used for a company. Returns null if none exist.
     */
    @Query("SELECT MAX(r.CNumber) FROM RTIMaster r WHERE r.companyRefId = :companyRefId")
    Integer findMaxCNumberByCompanyRefId(@Param("companyRefId") Integer companyRefId);
}

