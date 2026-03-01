package my.maleva.api.service;

import my.maleva.api.dto.SaleCreditMasterDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SaleCreditMasterService
 * Business logic interface for SaleCreditMaster operations
 */
public interface SaleCreditMasterService {

    /**
     * Get all SaleCreditMaster records by company ID
     */
    List<SaleCreditMasterDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get SaleCreditMaster records by company and status
     */
    List<SaleCreditMasterDto> getByCompanyIdAndStatus(Integer companyRefId, Integer cStatus);

    /**
     * Get SaleCreditMaster by ID
     */
    Optional<SaleCreditMasterDto> getById(Integer id);

    /**
     * Create new SaleCreditMaster record
     */
    SaleCreditMasterDto create(SaleCreditMasterDto dto);

    /**
     * Update SaleCreditMaster record
     */
    SaleCreditMasterDto update(Integer id, SaleCreditMasterDto dto);

    /**
     * Delete SaleCreditMaster record
     */
    boolean delete(Integer id);

    /**
     * Get SaleCreditMaster records by customer ID
     */
    List<SaleCreditMasterDto> getByCustomerRefId(Integer customerRefId);

    /**
     * Get SaleCreditMaster records by company and customer ID
     */
    List<SaleCreditMasterDto> getByCompanyAndCustomer(Integer companyRefId, Integer customerRefId);

    /**
     * Get SaleCreditMaster records by date range
     */
    List<SaleCreditMasterDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get SaleCreditMaster by reference number
     */
    Optional<SaleCreditMasterDto> getByRefNumber(Integer companyRefId, String refNumber);

    /**
     * Get SaleCreditMaster by C Number
     */
    Optional<SaleCreditMasterDto> getByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Get SaleCreditMaster records by employee ID
     */
    List<SaleCreditMasterDto> getByEmployeeId(Integer employeeRefId);

    /**
     * Get SaleCreditMaster records by company and employee
     */
    List<SaleCreditMasterDto> getByCompanyAndEmployee(Integer companyRefId, Integer employeeRefId);

    /**
     * Get SaleCreditMaster records by user ID
     */
    List<SaleCreditMasterDto> getByUserId(Integer userRefId);

    /**
     * Get SaleCreditMaster records by Sale Master Reference ID
     */
    List<SaleCreditMasterDto> getBySaleMasterRefId(Integer saleMasterRefId);

    /**
     * Count SaleCreditMaster by company
     */
    long countByCompanyId(Integer companyRefId);

    /**
     * Count SaleCreditMaster by company and status
     */
    long countByCompanyIdAndStatus(Integer companyRefId, Integer cStatus);

    /**
     * Count SaleCreditMaster by customer
     */
    long countByCustomerRefId(Integer customerRefId);

    /**
     * Check if reference number exists
     */
    boolean existsByRefNumber(Integer companyRefId, String refNumber);

    /**
     * Check if C Number exists
     */
    boolean existsByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Get SaleCreditMaster records by date range and status
     */
    List<SaleCreditMasterDto> getByDateAndStatus(Integer companyRefId, LocalDateTime startDate,
                                                  LocalDateTime endDate, Integer cStatus);

    /**
     * Change SaleCreditMaster status
     */
    SaleCreditMasterDto changeStatus(Integer id, Integer newStatus);

    /**
     * Activate SaleCreditMaster (set status to active)
     */
    SaleCreditMasterDto activate(Integer id);

    /**
     * Deactivate SaleCreditMaster (set status to inactive)
     */
    SaleCreditMasterDto deactivate(Integer id);
}

