package my.maleva.api.module.transaction.service;

import my.maleva.api.module.transaction.dto.PreAlertReportModel;
import my.maleva.api.module.transaction.dto.PreAlertSearchModel;
import my.maleva.api.module.transaction.dto.PreAlertMasterDto;
import my.maleva.api.module.transaction.dto.F5Dto;
import my.maleva.api.module.transaction.dto.TransactionDto;

import java.util.List;

/**
 * Service interface for Pre-Alert Report functionality
 * Defines contract for pre-alert report operations
 */
public interface PreAlertReportService {

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

    // Methods migrated from C# .NET implementation

    /**
     * Insert a list of PreAlert masters
     * @param objBrand List of PreAlert masters to insert
     * @param comId Company ID
     * @return Object containing success status, message, name, and id
     */
    Object insertPreAlert(List<PreAlertMasterDto> objBrand, Integer comId);

    /**
     * Edit a PreAlert record
     * @param id PreAlert ID
     * @param preAlertNo PreAlert Number
     * @param comId Company ID
     * @return Object containing success status, message, and data
     */
    Object editPreAlert(Integer id, Integer preAlertNo, Integer comId);

    /**
     * Select PreAlerts based on F5 view model
     * @param objlist F5 view model for filtering
     * @return Object containing success status, message, and data
     */
    Object selectPreAlert(F5Dto objlist);

    /**
     * Get Max PreAlert Report Number
     * @param obj F5 view model for filtering
     * @return Max report number
     */
    Object maxPreAlertReportNo(F5Dto obj);

    /**
     * Fetch PreAlert Report data using Transaction ViewModel
     * @param obj Transaction view model
     * @return Report data
     */
    Object preAlertReport(TransactionDto obj);
}
