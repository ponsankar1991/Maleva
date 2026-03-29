package my.maleva.api.module.salecreditmaster.repository;

import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SaleCreditMasterRepository
 * Spring Data JPA Repository for SaleCreditMaster entity
 * Provides CRUD operations and custom query methods
 */
@Repository
public interface SaleCreditMasterRepository extends JpaRepository<SaleCreditMaster, Integer> {

    /**
     * Find all SaleCreditMaster records by company ID
     */
    List<SaleCreditMaster> findByCompanyRefId(Integer companyRefId);

    /**
     * Find SaleCreditMaster records by company ID and C Status
     */
    List<SaleCreditMaster> findByCompanyRefIdAndCStatus(Integer companyRefId, Integer cStatus);

    /**
     * Find SaleCreditMaster records by customer ID
     */
    List<SaleCreditMaster> findByCustomerRefId(Integer customerRefId);

    /**
     * Find SaleCreditMaster records by company ID and customer ID
     */
    List<SaleCreditMaster> findByCompanyRefIdAndCustomerRefId(Integer companyRefId, Integer customerRefId);

    /**
     * Find SaleCreditMaster by reference number
     */
    Optional<SaleCreditMaster> findByCompanyRefIdAndRefNumber(Integer companyRefId, String refNumber);

    /**
     * Find SaleCreditMaster by C Number
     */
    Optional<SaleCreditMaster> findByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Find SaleCreditMaster records by date range
     */
    @Query("SELECT scm FROM SaleCreditMaster scm WHERE scm.companyRefId = :companyRefId " +
           "AND scm.saleDate BETWEEN :startDate AND :endDate ORDER BY scm.saleDate DESC")
    List<SaleCreditMaster> findByDateRange(@Param("companyRefId") Integer companyRefId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find SaleCreditMaster records by employee ID
     */
    List<SaleCreditMaster> findByEmployeeRefId(Integer employeeRefId);

    /**
     * Find SaleCreditMaster records by user ID
     */
    List<SaleCreditMaster> findByUserRefId(Integer userRefId);

    /**
     * Find SaleCreditMaster records by Sale Master Reference ID
     */
    List<SaleCreditMaster> findBySaleMasterRefId(Integer saleMasterRefId);

    /**
     * Count SaleCreditMaster by company
     */
    long countByCompanyRefId(Integer companyRefId);

    /**
     * Count SaleCreditMaster by company and status
     */
    long countByCompanyRefIdAndCStatus(Integer companyRefId, Integer cStatus);

    /**
     * Count SaleCreditMaster by customer
     */
    long countByCustomerRefId(Integer customerRefId);

    /**
     * Check if C Number exists
     */
    boolean existsByCompanyRefIdAndCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Check if reference number exists
     */
    boolean existsByCompanyRefIdAndRefNumber(Integer companyRefId, String refNumber);

    /**
     * Find SaleCreditMaster records by date and company with specific status
     */
    @Query("SELECT scm FROM SaleCreditMaster scm WHERE scm.companyRefId = :companyRefId " +
           "AND scm.saleDate >= :startDate AND scm.saleDate <= :endDate " +
           "AND scm.cStatus = :cStatus ORDER BY scm.saleDate DESC")
    List<SaleCreditMaster> findByDateAndStatus(@Param("companyRefId") Integer companyRefId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               @Param("cStatus") Integer cStatus);

    /**
     * Find SaleCreditMaster records by company and employee
     */
    List<SaleCreditMaster> findByCompanyRefIdAndEmployeeRefId(Integer companyRefId, Integer employeeRefId);
}

