package my.maleva.api.module.transaction.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.transaction.dto.*;
import my.maleva.api.module.transaction.service.PreAlertReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Pre-Alert Report operations
 * Provides endpoints for viewing, filtering, and exporting pre-alert reports
 * Equivalent to PreAlertReportView controller method in C# implementation
 */
@Slf4j
@RestController
@RequestMapping("/api/transaction/pre-alert")
@RequiredArgsConstructor
public class PreAlertReportController {

    private final PreAlertReportService preAlertService;

    /**
     * Get pre-alert report with dynamic filtering
     * Equivalent to the C# PreAlertReportView method
     */
    @PostMapping("/report")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<PreAlertReportModel>>> getPreAlertReport(
            @RequestBody PreAlertSearchModel searchModel) {

        log.info("POST /api/transaction/pre-alert/report - comId={}, customerId={}, jobId={}",
                searchModel.getComId(), searchModel.getCustomerId(), searchModel.getJobId());

        try {
            // Validate required parameters
            if (searchModel == null || searchModel.getComId() == null || searchModel.getComId() <= 0) {
                log.warn("Invalid search model: missing or invalid comId");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Company ID (comId) is required and must be greater than 0"));
            }

            // Fetch report data
            List<PreAlertReportModel> reportData = preAlertService.getPreAlertReport(searchModel);

            if (reportData == null || reportData.isEmpty()) {
                log.info("No pre-alert data found for comId={}", searchModel.getComId());
                return ResponseEntity.ok(ApiResponse.success("No data available for the specified criteria", reportData));
            }

            log.info("Successfully retrieved {} pre-alert records", reportData.size());
            return ResponseEntity.ok(ApiResponse.success("Pre-alert report fetched successfully", reportData));

        } catch (Exception e) {
            log.error("Error fetching pre-alert report: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch pre-alert report. Please try again later."));
        }
    }

    /**
     * Get pre-alert report with pagination support
     * Useful for UI with pagination controls
     */
    @PostMapping("/report/paginated")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<PreAlertReportModel>>> getPreAlertReportPaginated(
            @RequestBody PreAlertSearchModel searchModel) {

        log.info("POST /api/transaction/pre-alert/report/paginated - comId={}, pageNo={}, pageSize={}",
                searchModel.getComId(), searchModel.getPageNo(), searchModel.getPageSize());

        try {
            if (searchModel == null || searchModel.getComId() == null || searchModel.getComId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Company ID (comId) is required"));
            }

            if (searchModel.getPageNo() == null || searchModel.getPageNo() < 1) {
                searchModel.setPageNo(1);
            }
            if (searchModel.getPageSize() == null || searchModel.getPageSize() < 1) {
                searchModel.setPageSize(20);
            }

            long totalCount = preAlertService.getPreAlertReportCount(searchModel);
            long totalPages = (totalCount + searchModel.getPageSize() - 1) / searchModel.getPageSize();

            List<PreAlertReportModel> reportData = preAlertService.getPreAlertReportPaginated(searchModel);

            Map<String, Object> meta = new HashMap<>();
            meta.put("totalRecords", totalCount);
            meta.put("pageNo", searchModel.getPageNo());
            meta.put("pageSize", searchModel.getPageSize());
            meta.put("totalPages", totalPages);

            log.info("Returning paginated data - page: {}/{}, records: {}",
                    searchModel.getPageNo(), totalPages, reportData.size());

            return ResponseEntity.ok(ApiResponse.success(
                    "Pre-alert report fetched successfully",
                    reportData,
                    meta
            ));

        } catch (Exception e) {
            log.error("Error fetching paginated pre-alert report: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch paginated pre-alert report. Please try again later."));
        }
    }

    /**
     * Get pre-alert report with GET method and query parameters
     * Alternative to POST for simple queries
     */
    @GetMapping("/report")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<List<PreAlertReportModel>>> getPreAlertReportGet(
            @RequestParam Integer comId,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer jobId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Boolean pickupDate,
            @RequestParam(required = false) Boolean eta,
            @RequestParam(required = false) Integer etaType,
            @RequestParam(required = false) Boolean deliveryDone,
            @RequestParam(required = false) String sPort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "SaleDate") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortOrder) {

        log.info("GET /api/transaction/pre-alert/report - comId={}", comId);

        try {
            if (comId == null || comId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Company ID (comId) is required and must be greater than 0"));
            }

            PreAlertSearchModel searchModel = PreAlertSearchModel.builder()
                    .comId(comId)
                    .customerId(customerId)
                    .jobId(jobId)
                    .fromDate(fromDate)
                    .toDate(toDate)
                    .pickupDate(pickupDate)
                    .eta(eta)
                    .etaType(etaType)
                    .deliveryDone(deliveryDone)
                    .sPort(sPort)
                    .search(search)
                    .sortBy(sortBy)
                    .sortOrder(sortOrder)
                    .build();

            List<PreAlertReportModel> reportData = preAlertService.getPreAlertReport(searchModel);

            if (reportData == null || reportData.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("No data available", reportData));
            }

            return ResponseEntity.ok(ApiResponse.success("Pre-alert report fetched successfully", reportData));

        } catch (Exception e) {
            log.error("Error fetching pre-alert report: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to fetch pre-alert report"));
        }
    }

    /**
     * Export pre-alert report to CSV format
     * Returns downloadable CSV file
     */
    @PostMapping("/report/export-csv")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> exportPreAlertReportCSV(
            @RequestBody PreAlertSearchModel searchModel) {

        log.info("POST /api/transaction/pre-alert/report/export-csv - comId={}", searchModel.getComId());

        try {
            if (searchModel == null || searchModel.getComId() == null || searchModel.getComId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Company ID (comId) is required"));
            }

            String csvData = preAlertService.exportPreAlertReportToCSV(searchModel);

            if (csvData == null || csvData.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("No data to export"));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "pre-alert-report.csv");

            log.info("Successfully exported pre-alert report to CSV");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);

        } catch (Exception e) {
            log.error("Error exporting pre-alert report to CSV: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to export pre-alert report"));
        }
    }

    /**
     * Get pre-alert report count for pagination calculation
     */
    @PostMapping("/report/count")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Long>> getPreAlertReportCount(
            @RequestBody PreAlertSearchModel searchModel) {

        log.info("POST /api/transaction/pre-alert/report/count - comId={}", searchModel.getComId());

        try {
            if (searchModel == null || searchModel.getComId() == null || searchModel.getComId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Company ID (comId) is required"));
            }

            long count = preAlertService.getPreAlertReportCount(searchModel);

            return ResponseEntity.ok(ApiResponse.success(
                    "Pre-alert record count fetched successfully",
                    count
            ));

        } catch (Exception e) {
            log.error("Error counting pre-alert records: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to count pre-alert records"));
        }
    }

    /**
     * Health check endpoint for pre-alert service
     */
    @GetMapping("/health")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Pre-Alert service is running", "OK"));
    }

    // =========================================================================================
    // Endpoints migrated from C# .NET implementation
    // =========================================================================================

    @PostMapping("/insert")
    @PreAuthorize("permitAll()") // Adjust as needed
    public ResponseEntity<?> insertPreAlert(
            @RequestBody List<PreAlertMasterDto> objBrand,
            HttpServletRequest request) {
        try {
            String comIdHeader = request.getHeader("Comid");
            if (comIdHeader == null || comIdHeader.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("ok", false, "message", "Missing Comid header"));
            }
            Integer comId = Integer.parseInt(comIdHeader);
            
            Object result = preAlertService.insertPreAlert(objBrand, comId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error in InsertPreAlert", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/edit")
    @PreAuthorize("permitAll()") // Adjust as needed
    public ResponseEntity<?> editPreAlert(
            @RequestParam("Id") Integer id,
            @RequestParam("PreAlertNo") Integer preAlertNo,
            @RequestParam("Comid") Integer comId) {
        try {
            Object result = preAlertService.editPreAlert(id, preAlertNo, comId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error in EditPreAlert", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/select")
    @PreAuthorize("permitAll()") // Adjust as needed
    public ResponseEntity<?> selectPreAlert(@RequestBody F5Dto objlist) {
        try {
            Object result = preAlertService.selectPreAlert(objlist);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error in SelectPreAlert", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/max-no")
    @PreAuthorize("permitAll()") // Adjust as needed
    public ResponseEntity<?> maxPreAlertReportNo(@RequestBody F5Dto obj) {
        try {
            Object result = preAlertService.maxPreAlertReportNo(obj);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error in MaxPreAlertReportNo", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "error", ex.getMessage()));
        }
    }

    @PostMapping("/transaction-report")
    @PreAuthorize("permitAll()") // Adjust as needed
    public ResponseEntity<?> preAlertReportFromTransaction(@RequestBody TransactionDto obj, HttpServletRequest request) {
        try {
            Object result = preAlertService.preAlertReport(obj);
            
            if (result instanceof Map) {
                Map<String, Object> mapResult = (Map<String, Object>) result;
                Boolean isOk = (Boolean) mapResult.get("ok");
                if (Boolean.TRUE.equals(isOk)) {
                    // Equivalent to this.HttpContext.Session["reportdata"] = ro.Data1;
                    request.getSession().setAttribute("reportdata", mapResult.get("data"));
                } else {
                    request.getSession().setAttribute("reportdata", "");
                }
            }
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error in preAlertReportFromTransaction", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ok", false, "error", ex.getMessage()));
        }
    }
}
