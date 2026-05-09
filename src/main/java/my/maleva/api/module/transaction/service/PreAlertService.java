package my.maleva.api.module.transaction.service;

import my.maleva.api.module.transaction.dto.PreAlertDto;
import my.maleva.api.module.transaction.dto.PreAlertReportModel;
import my.maleva.api.module.transaction.dto.PreAlertSearchModel;

import java.util.List;

/**
 * Service interface for Pre-Alert functionality
 * Includes both report operations and CRUD operations for PreAlert entities
 */
public interface PreAlertService {

    /**
     * REPORT OPERATIONS
     */

    /**
     * Get pre-alert report data based on search criteria
     * Implements filtering, sorting, and pagination
     *
     * @param searchModel Filter criteria
     * @return List of pre-alert report records
     */
    List<PreAlertReportModel> getPreAlertReport(PreAlertSearchModel searchModel);

    /**
     * Get pre-alert report with pagination information
     * Useful for UI with pagination support
     *
     * @param searchModel Filter criteria including page and size
     * @return List of pre-alert report records for the requested page
     */
    List<PreAlertReportModel> getPreAlertReportPaginated(PreAlertSearchModel searchModel);

    /**
     * Get pre-alert report count for a given search criteria
     * Useful for calculating total pages
     *
     * @param searchModel Filter criteria
     * @return Total count of matching records
     */
    long getPreAlertReportCount(PreAlertSearchModel searchModel);

    /**
     * Export pre-alert report to CSV format (optional utility method)
     *
     * @param searchModel Filter criteria
     * @return CSV formatted data
     */
    String exportPreAlertReportToCSV(PreAlertSearchModel searchModel);

    /**
     * CRUD OPERATIONS FOR PREALERT ENTITIES
     */

    /**
     * Get all PreAlert records by PreAlertMaster ID
     */
    List<PreAlertDto> getByPreAlertMasterId(Integer preAlertMasterRefId);

    /**
     * Get count of records by PreAlertMaster ID
     */
    Long countByPreAlertMasterId(Integer preAlertMasterRefId);
}
