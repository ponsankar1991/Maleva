package my.maleva.api.module.employee.repository;

import my.maleva.api.module.employee.entity.SalaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SalaryEntryRepository
 * Spring Data JPA Repository for SalaryEntry entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SalaryEntryRepository extends JpaRepository<SalaryEntry, Integer> {

    /**
     * Find all SalaryEntry records by company ID
     */
    List<SalaryEntry> findByCompanyRefId(Integer companyRefId);

    /**
     * Find active SalaryEntry records by company ID
     */
    List<SalaryEntry> findByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Find SalaryEntry records by employee ID
     */
    List<SalaryEntry> findByEmployeeRefId(Integer employeeRefId);

    /**
     * Find SalaryEntry records by employee ID and company ID
     */
    List<SalaryEntry> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);

    /**
     * Find SalaryEntry records by date range
     */
    @Query("SELECT se FROM SalaryEntry se WHERE se.companyRefId = :companyRefId " +
           "AND se.saleDate BETWEEN :startDate AND :endDate ORDER BY se.saleDate DESC")
    List<SalaryEntry> findByDateRange(@Param("companyRefId") Integer companyRefId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find SalaryEntry by reference number
     */
    Optional<SalaryEntry> findByCompanyRefIdAndRefNumber(Integer companyRefId, String refNumber);

    /**
     * Find SalaryEntry records by bank ID
     */
    List<SalaryEntry> findByBankRefId(Integer bankRefId);

    /**
     * Find SalaryEntry records by PV Status
     */
    List<SalaryEntry> findByCompanyRefIdAndPvStatus(Integer companyRefId, Integer pvStatus);

    /**
     * Count SalaryEntry by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count active SalaryEntry by company
     */
    long countByCompanyRefIdAndActive(Integer companyRefId, Integer active);

    /**
     * Count SalaryEntry by employee
     */
    long countByEmployeeRefId(Integer employeeRefId);

    /**
     * Check if record exists by reference number
     */
    boolean existsByCompanyRefIdAndRefNumber(Integer companyRefId, String refNumber);

    /**
     * Check if record exists by C Number
     */
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find records by C Number
     */
    Optional<SalaryEntry> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find records by date and company with specific status
     */
    @Query("SELECT se FROM SalaryEntry se WHERE se.companyRefId = :companyRefId " +
           "AND se.saleDate >= :startDate AND se.saleDate <= :endDate " +
           "AND se.pvStatus = :pvStatus ORDER BY se.saleDate DESC")
    List<SalaryEntry> findByDateAndStatus(@Param("companyRefId") Integer companyRefId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("pvStatus") Integer pvStatus);
}

