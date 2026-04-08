package my.maleva.api.module.rti.service;

import my.maleva.api.module.rti.dto.RTIJobLookupDto;
import my.maleva.api.module.rti.dto.RTIMasterDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RTIMasterService
 * Business logic interface for RTIMaster operations
 */
public interface RTIMasterService {

    /**
     * Get all RTIMaster records by company ID
     */
    List<RTIMasterDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get active RTIMaster records by company ID
     */
    List<RTIMasterDto> getActiveByCompanyId(Integer companyRefId);

    /**
     * Get RTIMaster by ID
     */
    Optional<RTIMasterDto> getById(Integer id);

    /**
     * Create new RTIMaster record
     */
    RTIMasterDto create(RTIMasterDto dto);

    /**
     * Update RTIMaster record
     */
    RTIMasterDto update(Integer id, RTIMasterDto dto);

    /**
     * Delete RTIMaster record
     */
    boolean delete(Integer id);

    /**
     * Get RTIMaster by CNumber
     */
    Optional<RTIMasterDto> getByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Get RTIMaster by employee
     */
    List<RTIMasterDto> getByEmployee(Integer companyRefId, Integer employeeRefId);

    /**
     * Get RTIMaster by agent
     */
    List<RTIMasterDto> getByAgent(Integer companyRefId, Integer agentMasterRefId);

    /**
     * Get RTIMaster by date range
     */
    List<RTIMasterDto> getByDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get RTIMaster by CNumberDisplay
     */
    Optional<RTIMasterDto> getByCNumberDisplay(String cNumberDisplay);

    /**
     * Get sleeping RTI records
     */
    List<RTIMasterDto> getSleepingRecords(Integer companyRefId);

    /**
     * Get RTIMaster by truck
     */
    List<RTIMasterDto> getByTruck(Integer companyRefId, Integer truckRefId);

    /**
     * Find one or more sale orders by exact job number for RTI grid fill.
     */
    List<RTIJobLookupDto> searchJobNo(Integer companyRefId, String jobNo);

    /**
     * Check if CNumber exists
     */
    boolean existsByCNumber(Integer companyRefId, Integer cNumber);

    /**
     * Count RTIMaster by company
     */
    long countByCompanyId(Integer companyRefId);

    /**
     * Count active RTIMaster by company
     */
    long countActiveByCompanyId(Integer companyRefId);

    /**
     * Activate RTIMaster
     */
    RTIMasterDto activate(Integer id);

    /**
     * Deactivate RTIMaster
     */
    RTIMasterDto deactivate(Integer id);

    /**
     * Generate CNumberDisplay
     */
    String generateCNumberDisplay(Integer cNumber);
}

