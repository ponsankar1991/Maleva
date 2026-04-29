package my.maleva.api.module.transaction.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.transaction.dto.*;
import my.maleva.api.module.transaction.repository.PreAlertReportRepository;
import my.maleva.api.module.transaction.service.PreAlertReportService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for Pre-Alert Report functionality
 * Handles business logic and orchestration for pre-alert operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreAlertReportServiceImpl implements PreAlertReportService {

    private final PreAlertReportRepository preAlertRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper; // Reverted to constructor injection

    /**
     * Get pre-alert report data based on search criteria
     * Implements all filtering, sorting, and pagination logic
     */
    @Override
    public List<PreAlertReportModel> getPreAlertReport(PreAlertSearchModel searchModel) {
        log.info("Fetching pre-alert report for comId={}, customerId={}, jobId={}",
                searchModel.getComId(), searchModel.getCustomerId(), searchModel.getJobId());

        try {
            // Validate input
            if (searchModel == null || searchModel.getComId() == null || searchModel.getComId() <= 0) {
                log.warn("Invalid search model or comId provided");
                return Collections.emptyList();
            }

            // Fetch data from repository
            List<PreAlertReportModel> results = preAlertRepository.getPreAlertReportData(searchModel);

            if (results == null || results.isEmpty()) {
                log.warn("No pre-alert report data found for the given criteria");
                return Collections.emptyList();
            }

            log.info("Successfully retrieved {} pre-alert records", results.size());
            return results;

        } catch (Exception e) {
            log.error("Error fetching pre-alert report for comId={}: {}",
                    searchModel.getComId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get pre-alert report with pagination support
     */
    @Override
    public List<PreAlertReportModel> getPreAlertReportPaginated(PreAlertSearchModel searchModel) {
        log.info("Fetching paginated pre-alert report - page: {}, size: {}",
                searchModel.getPageNo(), searchModel.getPageSize());

        try {
            // Get all results first
            List<PreAlertReportModel> allResults = getPreAlertReport(searchModel);

            if (allResults.isEmpty()) {
                return Collections.emptyList();
            }

            // Apply pagination if pageNo and pageSize are provided
            if (searchModel.getPageNo() != null && searchModel.getPageSize() != null
                    && searchModel.getPageNo() > 0 && searchModel.getPageSize() > 0) {

                int pageNo = searchModel.getPageNo() - 1; // Convert to 0-based index
                int pageSize = searchModel.getPageSize();
                int startIndex = pageNo * pageSize;
                int endIndex = Math.min(startIndex + pageSize, allResults.size());

                if (startIndex >= allResults.size()) {
                    log.warn("Page number {} is out of range", searchModel.getPageNo());
                    return Collections.emptyList();
                }

                return allResults.subList(startIndex, endIndex);
            }

            // Return all results if pagination not requested
            return allResults;

        } catch (Exception e) {
            log.error("Error fetching paginated pre-alert report: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get count of pre-alert records matching search criteria
     * Useful for calculating total pages in pagination
     */
    @Override
    public long getPreAlertReportCount(PreAlertSearchModel searchModel) {
        log.debug("Counting pre-alert records for comId={}", searchModel.getComId());

        try {
            List<PreAlertReportModel> results = getPreAlertReport(searchModel);
            return results.size();

        } catch (Exception e) {
            log.error("Error counting pre-alert records: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Export pre-alert report to CSV format
     * Optional utility method for report export functionality
     */
    @Override
    public String exportPreAlertReportToCSV(PreAlertSearchModel searchModel) {
        log.info("Exporting pre-alert report to CSV for comId={}", searchModel.getComId());

        try {
            List<PreAlertReportModel> results = getPreAlertReport(searchModel);

            if (results.isEmpty()) {
                log.warn("No data to export");
                return ""; // Return empty CSV header
            }

            // Build CSV header
            StringBuilder csv = new StringBuilder();
            csv.append("Sale Date,Job No,Loading Vessel,Offvessell,Commodity,SCN,LSCN,");
            csv.append("Origin,Destination,S.Port,O.Port,Pickup Date,Delivery Date,");
            csv.append("ETA,ETB,ETD,OETA,OETB,OETD,Customer,Job Status,Employee,");
            csv.append("Agent Name,Agent Phone,O.Agent Name,O.Agent Phone,Remarks\n");

            // Add data rows
            for (PreAlertReportModel model : results) {
                csv.append(escapeCSV(model.getSaleDate().toString())).append(",");
                csv.append(escapeCSV(model.getJobNo())).append(",");
                csv.append(escapeCSV(model.getLoadingVesselName())).append(",");
                csv.append(escapeCSV(model.getOffVesselName())).append(",");
                csv.append(escapeCSV(model.getCommodity())).append(",");
                csv.append(escapeCSV(model.getScn())).append(",");
                csv.append(escapeCSV(model.getLscn())).append(",");
                csv.append(escapeCSV(model.getOrigin())).append(",");
                csv.append(escapeCSV(model.getDestination())).append(",");
                csv.append(escapeCSV(model.getSPort())).append(",");
                csv.append(escapeCSV(model.getOPort())).append(",");
                csv.append(escapeCSV(model.getPickupDate().toString())).append(",");
                csv.append(escapeCSV(model.getDeliveryDate().toString())).append(",");
                csv.append(escapeCSV(model.getEta().toString())).append(",");
                csv.append(escapeCSV(model.getEtb().toString())).append(",");
                csv.append(escapeCSV(model.getEtd().toString())).append(",");
                csv.append(escapeCSV(model.getOeta().toString())).append(",");
                csv.append(escapeCSV(model.getOetb().toString())).append(",");
                csv.append(escapeCSV(model.getOetd().toString())).append(",");
                csv.append(escapeCSV(model.getCustomerName())).append(",");
                csv.append(escapeCSV(model.getJobStatus())).append(",");
                csv.append(escapeCSV(model.getEmployeeName())).append(",");
                csv.append(escapeCSV(model.getAgentName())).append(",");
                csv.append(escapeCSV(model.getAgentPhone())).append(",");
                csv.append(escapeCSV(model.getOAgentName())).append(",");
                csv.append(escapeCSV(model.getOAgentPhone())).append(",");
                csv.append(escapeCSV(model.getRemarks())).append("\n");
            }

            log.info("Successfully exported {} records to CSV", results.size());
            return csv.toString();

        } catch (Exception e) {
            log.error("Error exporting pre-alert report to CSV: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Helper method to escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
    
    @Override
    @Transactional
    public Object insertPreAlert(List<PreAlertMasterDto> objBrand, Integer comId) {
        log.info("Inserting {} PreAlert master records via SP_PreAlert for comId: {}", objBrand.size(), comId);
        
        Map<String, Object> response = new HashMap<>();
        try {
            // Convert list to JSON string to match @master parameter in SP_PreAlert
            String masterJson = objectMapper.writeValueAsString(objBrand);
            
            // Setup SimpleJdbcCall to call the stored procedure SP_PreAlert
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("SP_PreAlert")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("@master", Types.NVARCHAR),
                            new SqlParameter("@ComId", Types.INTEGER)
                    );
            
            Map<String, Object> inParams = new HashMap<>();
            inParams.put("@master", masterJson);
            inParams.put("@ComId", comId);

            Map<String, Object> out = jdbcCall.execute(inParams);
            
            // Handle result set from stored procedure
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resultList = (List<Map<String, Object>>) out.get("#result-set-1");
            if (resultList != null && !resultList.isEmpty()) {
                Map<String, Object> row = resultList.get(0);
                Integer resultStatus = (Integer) row.get("Result");
                String msg = (String) row.get("Msg");
                String billNo = (String) row.get("BillNo");
                Integer id = (Integer) row.get("Id");
                
                if (resultStatus != null && resultStatus == 1) {
                    response.put("ok", true);
                    response.put("message", msg);
                    response.put("Name", billNo); // Matches Data1 in C#
                    response.put("Id", id);       // Matches Data2 in C#
                } else {
                    response.put("ok", false);
                    response.put("message", msg);
                }
            } else {
                response.put("ok", false);
                response.put("message", "No result returned from database procedure.");
            }
            
        } catch (Exception ex) {
            log.error("Error executing insertPreAlert via SP_PreAlert", ex);
            response.put("ok", false);
            response.put("error", ex.getMessage());
        }
        
        return response;
    }

    @Override
    public Object editPreAlert(Integer id, Integer preAlertNo, Integer comId) {
        log.info("Editing PreAlert with id: {}, preAlertNo: {}, comId: {}", id, preAlertNo, comId);
        // TODO: Implement actual business logic for editing a pre-alert
        // This is a placeholder implementation
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("message", "Successfully edited pre-alert.");
        response.put("Data", "Some data for the edited pre-alert"); // Placeholder
        return response;
    }

    @Override
    public Object selectPreAlert(F5Dto objlist) {
        log.info("Selecting PreAlerts with filter: {}", objlist);
        // TODO: Implement actual business logic for selecting pre-alerts
        // This is a placeholder implementation
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("message", "Successfully selected pre-alerts.");
        response.put("Data", Collections.emptyList()); // Placeholder
        return response;
    }

    @Override
    public Object maxPreAlertReportNo(F5Dto obj) {
        log.info("Fetching max PreAlert report number with filter: {}", obj);
        // TODO: Implement actual business logic for getting max report number
        // This is a placeholder implementation
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("No", 9999); // Placeholder
        return response;
    }

    @Override
    public Object preAlertReport(TransactionDto obj) {
        log.info("Generating PreAlert report for transaction: {}", obj);
        // TODO: Implement actual business logic for generating the report
        // This is a placeholder implementation
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("data", "Report data would be here."); // Placeholder
        return response;
    }
}
