package my.maleva.api.module.transaction.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.transaction.dto.PreAlertReportModel;
import my.maleva.api.module.transaction.dto.PreAlertSearchModel;
import my.maleva.api.module.transaction.dto.F5Dto;
import my.maleva.api.module.transaction.dto.TransactionDto;
import my.maleva.api.module.transaction.dto.PreAlertMasterDto;
import my.maleva.api.module.transaction.dto.PreAlertDto;
import my.maleva.api.module.transaction.service.PreAlertReportService;
import my.maleva.api.module.transaction.service.PreAlertMasterService;
import my.maleva.api.module.transaction.service.PreAlertService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Pre-Alert operations
 * Handles HTTP requests for pre-alert report data and master CRUD operations
 * Base URL: /api/transaction/pre-alert
 */
@Slf4j
@RestController
@RequestMapping("/api/transaction/pre-alert")
@RequiredArgsConstructor
public class PreAlertController {

    private final PreAlertService preAlertService;
    private final PreAlertReportService preAlertReportService;
    private final PreAlertMasterService preAlertMasterService;

    /**
     * Helper method to create error response
     */
    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("ok", false);
        errorResponse.put("message", message);
        errorResponse.put("data", null);
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Helper method to validate company ID
     */
    private boolean isValidCompanyId(Integer comId) {
        return comId != null && comId > 0;
    }

    private Integer resolveCompanyId(Integer queryComId, Integer headerComId) {
        return isValidCompanyId(queryComId) ? queryComId : headerComId;
    }

    /**
     * REPORT OPERATIONS
     */

    /**
     * Get pre-alert report with filtering and pagination
     * POST /api/transaction/pre-alert/report
     */
    @PostMapping("/report")
    public ResponseEntity<Object> getPreAlertReport(@RequestBody PreAlertSearchModel searchModel) {
        log.info("Received pre-alert report request - comId: {}, customerId: {}, jobId: {}, fromDate: {}, toDate: {}, eta: {}, pickupDate: {}",
                searchModel.getComId(), searchModel.getCustomerId(), searchModel.getJobId(),
                searchModel.getFromDate(), searchModel.getToDate(), searchModel.getEta(), searchModel.getPickupDate());

        if (searchModel == null || !isValidCompanyId(searchModel.getComId())) {
            log.warn("Invalid search model or missing comId");
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Company ID is required");
        }

        try {
            List<PreAlertReportModel> reportData = preAlertService.getPreAlertReport(searchModel);

            // Apply sorting logic based on C# implementation
            // Sort by DETA when ETA filter is active, otherwise sort by SaleDate
            final boolean sortByDeta = searchModel.getEta() != null && searchModel.getEta();
            reportData.sort((a, b) -> {
                if (sortByDeta) {
                    String detaA = a.getDeta() != null ? a.getDeta() : "";
                    String detaB = b.getDeta() != null ? b.getDeta() : "";
                    return detaA.compareTo(detaB);
                } else {
                    String dateA = a.getSaleDate() != null ? a.getSaleDate() : "";
                    String dateB = b.getSaleDate() != null ? b.getSaleDate() : "";
                    return dateA.compareTo(dateB);
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("message", reportData.isEmpty() ? "No records found" : "Success");
            response.put("data", reportData);

            log.info("Pre-alert report retrieved successfully - record count: {}", reportData.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving pre-alert report: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving pre-alert report: " + e.getMessage());
        }
    }

    /**
     * Get pre-alert report with pagination support
     * POST /api/transaction/pre-alert/report-paginated
     */
    @PostMapping("/report-paginated")
    public ResponseEntity<Object> getPreAlertReportPaginated(@RequestBody PreAlertSearchModel searchModel) {
        log.info("Received paginated pre-alert report request");

        if (searchModel == null || !isValidCompanyId(searchModel.getComId())) {
            log.warn("Invalid search model or missing comId");
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Company ID is required");
        }

        try {
            List<PreAlertReportModel> reportData = preAlertService.getPreAlertReportPaginated(searchModel);
            long totalCount = preAlertService.getPreAlertReportCount(searchModel);

            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("message", "Success");
            response.put("data", reportData);
            response.put("totalCount", totalCount);

            log.info("Paginated pre-alert report retrieved - records: {}, total: {}",
                    reportData.size(), totalCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving paginated pre-alert report: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving paginated pre-alert report: " + e.getMessage());
        }
    }

    /**
     * Export pre-alert report to CSV format
     * POST /api/transaction/pre-alert/export-csv
     */
    @PostMapping("/export-csv")
    public ResponseEntity<Object> exportPreAlertReportToCSV(@RequestBody PreAlertSearchModel searchModel) {
        log.info("Received CSV export request for pre-alert report - comId: {}", searchModel.getComId());

        if (searchModel == null || !isValidCompanyId(searchModel.getComId())) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Company ID is required");
        }

        try {
            String csvData = preAlertService.exportPreAlertReportToCSV(searchModel);

            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("message", "CSV export successful");
            response.put("data", csvData);

            log.info("CSV export completed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error exporting pre-alert report to CSV: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error exporting to CSV: " + e.getMessage());
        }
    }

    /**
     * Get count of pre-alert records matching search criteria
     * POST /api/transaction/pre-alert/count
     */
    @PostMapping("/count")
    public ResponseEntity<Object> getPreAlertReportCount(@RequestBody PreAlertSearchModel searchModel) {
        log.info("Received count request for pre-alert report - comId: {}", searchModel.getComId());

        if (searchModel == null || !isValidCompanyId(searchModel.getComId())) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Company ID is required");
        }

        try {
            long count = preAlertService.getPreAlertReportCount(searchModel);

            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("message", "Success");
            response.put("count", count);

            log.info("Pre-alert record count retrieved: {}", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error counting pre-alert records: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error counting records: " + e.getMessage());
        }
    }

    /**
     * BUSINESS OPERATIONS
     */

    /**
     * Insert pre-alert master records
     * POST /api/transaction/pre-alert/insert
     */
    @PostMapping("/insert")
    public ResponseEntity<Object> insertPreAlert(@RequestBody List<PreAlertMasterDto> preAlertMasters,
                                                @RequestParam(required = false) Integer comId,
                                                @RequestHeader(value = "Comid", required = false) Integer headerComId) {
        Integer resolvedComId = resolveCompanyId(comId, headerComId);
        int requestCount = preAlertMasters == null ? 0 : preAlertMasters.size();
        log.info("Received insert pre-alert request - count: {}, comId: {}", requestCount, resolvedComId);

        if (!isValidCompanyId(resolvedComId)) {
            log.warn("Invalid company ID provided");
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Company ID is required");
        }

        if (preAlertMasters == null || preAlertMasters.isEmpty()) {
            log.warn("No pre-alert records supplied");
            return createErrorResponse(HttpStatus.BAD_REQUEST, "At least one pre-alert record is required");
        }

        try {
            Object result = preAlertReportService.insertPreAlert(preAlertMasters, resolvedComId);
            log.info("Pre-alert insert completed successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error inserting pre-alert: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error inserting pre-alert: " + e.getMessage());
        }
    }

    /**
     * Get pre-alert record for editing (display existing data)
     * GET /api/transaction/pre-alert/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public ResponseEntity<Object> getPreAlertForEdit(@PathVariable Integer id,
                                                     @RequestParam(required = false) Integer preAlertNo,
                                                     @RequestParam Integer comId) {
        log.info("Received get pre-alert for edit request - id: {}, preAlertNo: {}, comId: {}", id, preAlertNo, comId);

        try {
            Object result = preAlertReportService.editPreAlert(id, preAlertNo, comId);
            log.info("Pre-alert data retrieved for editing");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error retrieving pre-alert for edit: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving pre-alert: " + e.getMessage());
        }
    }

    /**
     * Update pre-alert record (via stored procedure)
     * PUT /api/transaction/pre-alert/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePreAlert(@PathVariable Integer id,
                                                @RequestBody PreAlertMasterDto masterDto,
                                                @RequestParam Integer comId) {
        log.info("Received update pre-alert request - id: {}, comId: {}", id, comId);

        if (masterDto == null) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, "PreAlert data is required");
        }

        // Ensure the ID matches
        masterDto.setId(id);

        try {
            Object result = preAlertReportService.updatePreAlert(masterDto, comId);
            log.info("Pre-alert update completed successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error updating pre-alert: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error updating pre-alert: " + e.getMessage());
        }
    }

    /**
     * Select pre-alerts based on filter criteria
     * POST /api/transaction/pre-alert/select
     */
    @PostMapping("/select")
    public ResponseEntity<Object> selectPreAlert(@RequestBody F5Dto filterModel) {
        log.info("Received select pre-alert request - comId: {}", filterModel.getComid());

        try {
            Object result = preAlertReportService.selectPreAlert(filterModel);
            log.info("Pre-alert select completed successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error selecting pre-alerts: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error selecting pre-alerts: " + e.getMessage());
        }
    }

    /**
     * Get maximum pre-alert report number
     * POST /api/transaction/pre-alert/max-report-no
     */
    @PostMapping({"/max-report-no", "/max-no"})
    public ResponseEntity<Object> maxPreAlertReportNo(@RequestBody F5Dto filterModel) {
        log.info("Received max pre-alert report number request - comId: {}", filterModel.getComid());

        try {
            Object result = preAlertReportService.maxPreAlertReportNo(filterModel);
            log.info("Max pre-alert report number retrieved successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error retrieving max pre-alert report number: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving max report number: " + e.getMessage());
        }
    }

    /**
     * Generate pre-alert report using transaction view model
     * POST /api/transaction/pre-alert/generate-report
     */
    @PostMapping("/generate-report")
    public ResponseEntity<Object> preAlertReport(@RequestBody TransactionDto transactionDto) {
        log.info("Received pre-alert report generation request - comId: {}", transactionDto.getComid());

        try {
            Object result = preAlertReportService.preAlertReport(transactionDto);
            log.info("Pre-alert report generated successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error generating pre-alert report: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error generating report: " + e.getMessage());
        }
    }

    /**
     * MASTER CRUD OPERATIONS
     */

    @GetMapping("/masters/company/{companyRefId}")
    public ResponseEntity<List<PreAlertMasterDto>> getAllByCompany(@PathVariable Integer companyRefId) {
        log.info("Fetching all PreAlert masters for company: {}", companyRefId);
        return ResponseEntity.ok(preAlertMasterService.getAllByCompanyId(companyRefId));
    }

    @GetMapping("/masters/company/{companyRefId}/active")
    public ResponseEntity<List<PreAlertMasterDto>> getActiveByCompany(@PathVariable Integer companyRefId) {
        log.info("Fetching active PreAlert masters for company: {}", companyRefId);
        return ResponseEntity.ok(preAlertMasterService.getActiveByCompanyId(companyRefId));
    }

    @GetMapping("/masters/company/{companyRefId}/date-range")
    public ResponseEntity<List<PreAlertMasterDto>> getByDateRange(
            @PathVariable Integer companyRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Fetching PreAlert masters for company: {}, fromDate: {}, toDate: {}", companyRefId, fromDate, toDate);
        return ResponseEntity.ok(preAlertMasterService.getByDateRange(companyRefId, fromDate, toDate));
    }

    @GetMapping("/masters/{id}")
    public ResponseEntity<PreAlertMasterDto> getById(@PathVariable Integer id) {
        return preAlertMasterService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/masters/customer/{customerMasterRefId}")
    public ResponseEntity<List<PreAlertMasterDto>> getByCustomer(@PathVariable Integer customerMasterRefId) {
        return ResponseEntity.ok(preAlertMasterService.getByCustomerId(customerMasterRefId));
    }

    @GetMapping("/masters/job-type/{jobTypeMasterRefId}")
    public ResponseEntity<List<PreAlertMasterDto>> getByJobType(@PathVariable Integer jobTypeMasterRefId) {
        return ResponseEntity.ok(preAlertMasterService.getByJobTypeId(jobTypeMasterRefId));
    }

    @GetMapping("/masters/port/{port}")
    public ResponseEntity<List<PreAlertMasterDto>> getByPort(@PathVariable String port) {
        return ResponseEntity.ok(preAlertMasterService.getByPort(port));
    }

    @GetMapping("/masters/vessel/{vessel}")
    public ResponseEntity<List<PreAlertMasterDto>> getByVessel(@PathVariable String vessel) {
        return ResponseEntity.ok(preAlertMasterService.getByVessel(vessel));
    }

    @GetMapping("/masters/cnumber/{cNumber}")
    public ResponseEntity<PreAlertMasterDto> getByCNumber(
            @PathVariable Integer cNumber,
            @RequestParam Integer companyRefId) {
        return preAlertMasterService.getByCNumber(cNumber, companyRefId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/masters/cnumber-display/{cNumberDisplay}")
    public ResponseEntity<PreAlertMasterDto> getByCNumberDisplay(@PathVariable String cNumberDisplay) {
        return preAlertMasterService.getByCNumberDisplay(cNumberDisplay)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/masters/{id}/details")
    public ResponseEntity<List<PreAlertDto>> getDetails(@PathVariable Integer id) {
        log.info("Fetching PreAlert details for master: {}", id);
        return ResponseEntity.ok(preAlertService.getByPreAlertMasterId(id));
    }

    @GetMapping("/masters/{id}/details/count")
    public ResponseEntity<Long> countDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(preAlertService.countByPreAlertMasterId(id));
    }

    @DeleteMapping("/masters/{id}")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        boolean deleted = preAlertMasterService.delete(id);
        if (!deleted) {
            return createErrorResponse(HttpStatus.NOT_FOUND, "PreAlert master not found");
        }
        return ResponseEntity.ok(Map.of("ok", true, "message", "PreAlert deleted"));
    }

    @PostMapping("/masters/{id}/activate")
    public ResponseEntity<PreAlertMasterDto> activate(@PathVariable Integer id) {
        return ResponseEntity.ok(preAlertMasterService.activate(id));
    }

    @PostMapping("/masters/{id}/deactivate")
    public ResponseEntity<PreAlertMasterDto> deactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(preAlertMasterService.deactivate(id));
    }

    @GetMapping("/masters/company/{companyRefId}/count-active")
    public ResponseEntity<Long> countActive(@PathVariable Integer companyRefId) {
        return ResponseEntity.ok(preAlertMasterService.countActiveRecords(companyRefId));
    }

    @PostMapping("/masters/bulk-import")
    public ResponseEntity<Object> bulkImport(
            @RequestParam String masterJson,
            @RequestParam Integer companyId) {
        preAlertMasterService.executePreAlertStoredProcedure(masterJson, companyId);
        return ResponseEntity.ok(Map.of("ok", true, "message", "Bulk import executed successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("ok", true, "message", "PreAlert service is healthy"));
    }
}
