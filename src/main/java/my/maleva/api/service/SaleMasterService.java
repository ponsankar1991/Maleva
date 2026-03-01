package my.maleva.api.service;

import my.maleva.api.dto.SaleMasterDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SaleMasterService
 * Business logic interface for SaleMaster operations
 * Incorporates SP_SaleMaster stored procedure business logic
 */
public interface SaleMasterService {

    // Core CRUD Operations
    List<SaleMasterDto> getAllByCompanyId(Integer companyRefId);
    List<SaleMasterDto> getByCompanyIdAndStatus(Integer companyRefId, Integer active);
    Optional<SaleMasterDto> getById(Integer id);
    SaleMasterDto create(SaleMasterDto dto);
    SaleMasterDto update(Integer id, SaleMasterDto dto);
    boolean delete(Integer id);

    // Retrieve by Reference
    List<SaleMasterDto> getByCustomerRefId(Integer customerRefId);
    List<SaleMasterDto> getByCompanyAndCustomer(Integer companyRefId, Integer customerRefId);
    Optional<SaleMasterDto> getByCNumber(Integer companyRefId, Integer cNumber);

    // Filter by Date Range
    List<SaleMasterDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);
    List<SaleMasterDto> getByDateAndStatus(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate, Integer active);

    // Filter by Employee/User
    List<SaleMasterDto> getByEmployeeId(Integer employeeRefId);
    List<SaleMasterDto> getByCompanyAndEmployee(Integer companyRefId, Integer employeeRefId);
    List<SaleMasterDto> getByUserId(Integer userRefId);

    // Filter by Type and Job
    List<SaleMasterDto> getByCompanyAndBillType(Integer companyRefId, String billType);
    List<SaleMasterDto> getByCompanyAndSaleType(Integer companyRefId, String saleType);
    List<SaleMasterDto> getByJobMasterRefId(Integer jobMasterRefId);

    // Filter by Agent and Driver
    List<SaleMasterDto> getByAgentMasterRefId(Integer agentMasterRefId);
    List<SaleMasterDto> getByDriverRefid(Integer driverRefid);

    // Count Operations
    long countByCompanyId(Integer companyRefId);
    long countByCompanyIdAndStatus(Integer companyRefId, Integer active);
    long countByCustomerRefId(Integer customerRefId);

    // Duplicate Check
    boolean existsByCNumber(Integer companyRefId, Integer cNumber);

    // Status Management
    SaleMasterDto activate(Integer id);
    SaleMasterDto deactivate(Integer id);

    // SP_SaleMaster Business Logic Operations
    /**
     * Process sale master batch import (from stored procedure logic)
     * Handles validation, duplicate checks, and amount calculations
     */
    void processSaleMasterBatch(List<SaleMasterDto> saleList, Integer companyId);

    /**
     * Calculate totals for sale master (Gross, Tax, Discount, etc.)
     */
    SaleMasterDto calculateSaleTotals(SaleMasterDto dto);

    /**
     * Validate sale data according to business rules
     */
    void validateSaleData(SaleMasterDto dto);
}

