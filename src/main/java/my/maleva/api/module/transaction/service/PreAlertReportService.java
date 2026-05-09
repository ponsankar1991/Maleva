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
     * Insert a list of PreAlert masters.
     * Creates new PreAlert master records with nested detail rows.
     *
     * Detail row handling for INSERT:
     * - Set PreAlertDto.Id = 0 or null for new detail rows (will INSERT)
     * - Set PreAlertDto.Id > 0 for existing detail rows (will UPDATE)
     * - Old PreAlert details are NOT deleted, only new rows are INSERTED/existing rows are UPDATED
     *
     * @param objBrand List of PreAlert masters with nested PreAlertDto detail rows to insert/update
     * @param comId Company ID
     * @return Object containing success status (ok=true/false), message, BillNo (PreAlert number), and Id
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
     * Update PreAlert record via stored procedure.
     * Updates existing PreAlert master and handles detail row changes.
     *
     * Detail row handling for UPDATE:
     * - Set PreAlertDto.Id = 0 or null for NEW detail rows (will INSERT)
     * - Set PreAlertDto.Id > 0 for EXISTING detail rows being modified (will UPDATE)
     * - Omit detail rows that should remain unchanged (they won't be modified)
     * - Old PreAlert details are NOT deleted automatically. To "delete", simply don't include them
     * - The SP does NOT support explicit DELETE of detail rows
     *
     * @param masterDto Updated PreAlert master (Id must be > 0) with detail rows to insert/update
     * @param comId Company ID
     * @return Response object with success status (ok=true/false), message, and Id
     */
    Object updatePreAlert(PreAlertMasterDto masterDto, Integer comId);

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
