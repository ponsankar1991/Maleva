package my.maleva.api.service;

import my.maleva.api.dto.PreAlertDto;
import java.util.List;
import java.util.Optional;

public interface PreAlertService {

    /**
     * Get all PreAlert records by company ID
     */
    List<PreAlertDto> getAllByCompanyId(Integer companyRefId);

    /**
     * Get active PreAlert records by company ID
     */
    List<PreAlertDto> getActiveByCompanyId(Integer companyRefId);

    /**
     * Get PreAlert by ID
     */
    Optional<PreAlertDto> getById(Integer id);

    /**
     * Create new PreAlert record
     */
    PreAlertDto create(PreAlertDto dto);

    /**
     * Update existing PreAlert record
     */
    PreAlertDto update(Integer id, PreAlertDto dto);

    /**
     * Delete PreAlert record
     */
    boolean delete(Integer id);

    /**
     * Get all PreAlert records by PreAlertMaster ID
     */
    List<PreAlertDto> getByPreAlertMasterId(Integer preAlertMasterRefId);

    /**
     * Get PreAlert records by customer ID
     */
    List<PreAlertDto> getByCustomerId(Integer customerMasterRefId);

    /**
     * Get PreAlert records by employee ID
     */
    List<PreAlertDto> getByEmployeeId(Integer employeeMasterRefId);

    /**
     * Get PreAlert records by job type ID
     */
    List<PreAlertDto> getByJobTypeId(Integer jobTypeMasterRefId);

    /**
     * Get PreAlert records by job status ID
     */
    List<PreAlertDto> getByJobStatusId(Integer jobStatusMasterRefId);

    /**
     * Get PreAlert records by boarding officer ID
     */
    List<PreAlertDto> getByBoardingOfficerId(Integer boardingOfficerRefId);

    /**
     * Get PreAlert records by vessel name
     */
    List<PreAlertDto> getByVessel(String vessel);

    /**
     * Get PreAlert records by port
     */
    List<PreAlertDto> getByPort(String port);

    /**
     * Get PreAlert records by job number
     */
    List<PreAlertDto> getByJobNo(String jobNo);

    /**
     * Delete all PreAlert records by PreAlertMaster ID
     */
    void deleteByPreAlertMasterId(Integer preAlertMasterRefId);

    /**
     * Get count of records by PreAlertMaster ID
     */
    Long countByPreAlertMasterId(Integer preAlertMasterRefId);

    /**
     * Activate PreAlert record
     */
    PreAlertDto activate(Integer id);

    /**
     * Deactivate PreAlert record
     */
    PreAlertDto deactivate(Integer id);
}

