package my.maleva.api.service;

import my.maleva.api.dto.PreAlertMasterDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PreAlertMasterService {

    /**
     * Get all PreAlertMaster records by company ID
     */
    List<PreAlertMasterDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get active PreAlertMaster records by company ID
     */
    List<PreAlertMasterDto> getActiveByCompanyId(Integer companyRefId);

    /**
     * Get PreAlertMaster by ID
     */
    Optional<PreAlertMasterDto> getById(Integer id);

    /**
     * Create new PreAlertMaster record
     */
    PreAlertMasterDto create(PreAlertMasterDto dto);

    /**
     * Update existing PreAlertMaster record
     */
    PreAlertMasterDto update(Integer id, PreAlertMasterDto dto);

    /**
     * Delete PreAlertMaster record
     */
    boolean delete(Integer id);

    /**
     * Get PreAlertMaster records by customer ID
     */
    List<PreAlertMasterDto> getByCustomerId(Integer customerMasterRefId);

    /**
     * Get PreAlertMaster records by job type ID
     */
    List<PreAlertMasterDto> getByJobTypeId(Integer jobTypeMasterRefId);

    /**
     * Get PreAlertMaster records by port
     */
    List<PreAlertMasterDto> getByPort(String port);

    /**
     * Get PreAlertMaster records by vessel name
     */
    List<PreAlertMasterDto> getByVessel(String vessel);

    /**
     * Get PreAlertMaster records within date range
     */
    List<PreAlertMasterDto> getByDateRange(Integer companyId, LocalDate fromDate, LocalDate toDate);

    /**
     * Get PreAlertMaster by CNumber
     */
    Optional<PreAlertMasterDto> getByCNumber(Integer cNumber, Integer companyRefId);

    /**
     * Get PreAlertMaster by CNumberDisplay
     */
    Optional<PreAlertMasterDto> getByCNumberDisplay(String cNumberDisplay);

    /**
     * Get count of active records
     */
    Long countActiveRecords(Integer companyRefId);

    /**
     * Activate PreAlertMaster record
     */
    PreAlertMasterDto activate(Integer id);

    /**
     * Deactivate PreAlertMaster record
     */
    PreAlertMasterDto deactivate(Integer id);

    /**
     * Execute SP_PreAlert stored procedure for bulk operations
     */
    void executePreAlertStoredProcedure(String masterJson, Integer companyId);
}

