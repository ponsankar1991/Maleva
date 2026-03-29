package my.maleva.api.module.employee.service;

import my.maleva.api.module.employee.dto.SalaryEntryDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SalaryEntryService
 * Business logic interface for SalaryEntry operations
 */
public interface SalaryEntryService {

    /**
     * Get all SalaryEntry records by company ID
     */
    List<SalaryEntryDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get active SalaryEntry records by company ID
     */
    List<SalaryEntryDto> getActiveByCompanyId(Integer companyRefId);

    /**
     * Get SalaryEntry by ID
     */
    Optional<SalaryEntryDto> getById(Integer id);

    /**
     * Create new SalaryEntry record
     */
    SalaryEntryDto create(SalaryEntryDto dto);

    /**
     * Update SalaryEntry record
     */
    SalaryEntryDto update(Integer id, SalaryEntryDto dto);

    /**
     * Delete SalaryEntry record
     */
    boolean delete(Integer id);

    /**
     * Get SalaryEntry records by employee ID
     */
    List<SalaryEntryDto> getByEmployeeId(Integer employeeRefId);

    /**
     * Get SalaryEntry records by employee ID and company ID
     */
    List<SalaryEntryDto> getByCompanyAndEmployee(Integer companyRefId, Integer employeeRefId);

    /**
     * Get SalaryEntry records by date range
     */
    List<SalaryEntryDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get SalaryEntry by reference number
     */
    Optional<SalaryEntryDto> getByRefNumber(Integer companyRefId, String refNumber);

    /**
     * Get SalaryEntry records by bank ID
     */
    List<SalaryEntryDto> getByBankId(Integer bankRefId);

    /**
     * Get SalaryEntry records by PV Status
     */
    List<SalaryEntryDto> getByPvStatus(Integer companyRefId, Integer pvStatus);

    /**
     * Count SalaryEntry by company
     */
    long countByCompanyId(Integer companyRefId);

    /**
     * Count active SalaryEntry by company
     */
    long countActiveByCompanyId(Integer companyRefId);

    /**
     * Count SalaryEntry by employee
     */
    long countByEmployeeId(Integer employeeRefId);

    /**
     * Check if reference number exists
     */
    boolean existsByRefNumber(Integer companyRefId, String refNumber);

    /**
     * Check if C Number exists
     */
    boolean existsByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Get SalaryEntry by C Number
     */
    Optional<SalaryEntryDto> getByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Get SalaryEntry records by date and status
     */
    List<SalaryEntryDto> getByDateAndStatus(Integer companyRefId, LocalDateTime startDate,
                                            LocalDateTime endDate, Integer pvStatus);

    /**
     * Activate SalaryEntry
     */
    SalaryEntryDto activate(Integer id);

    /**
     * Deactivate SalaryEntry
     */
    SalaryEntryDto deactivate(Integer id);

    /**
     * Update PV Status
     */
    SalaryEntryDto updatePvStatus(Integer id, Integer pvStatus);
}

