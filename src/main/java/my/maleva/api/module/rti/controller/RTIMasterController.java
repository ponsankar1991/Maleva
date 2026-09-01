package my.maleva.api.module.rti.controller;

import my.maleva.api.module.rti.dto.RTIJobLookupDto;
import my.maleva.api.module.rti.dto.RTIMasterDto;
import my.maleva.api.module.rti.dto.RtiSummaryResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;
import my.maleva.api.module.rti.service.RTIMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import my.maleva.api.common.dto.ApiResponse;

/**
 * RTIMaster REST Controller
 * Handles all RESTful API endpoints for RTIMaster operations
 * Base URL: /api/rti-masters
 */
@RestController
@RequestMapping("/api/rti-masters")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RTIMasterController {

    private static final Logger logger = LoggerFactory.getLogger(RTIMasterController.class);

    @Autowired
    private RTIMasterService rtiMasterService;

    @GetMapping("/company/{companyRefId}")
    @PermitAll
    public ResponseEntity<List<RTIMasterDto>> getAllByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching all RTIMaster records for company: {}", companyRefId);
        List<RTIMasterDto> records = rtiMasterService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/company/{companyRefId}/active")
    @PermitAll
    public ResponseEntity<List<RTIMasterDto>> getActiveByCompanyId(
            @PathVariable Integer companyRefId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer driverId,
            @RequestParam(required = false) Integer truckId,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) String search) {
        logger.info("Fetching active RTIMaster records for company: {} with filters", companyRefId);
        List<RTIMasterDto> records = rtiMasterService.getActiveByCompanyId(
                companyRefId,
                fromDate,
                toDate,
                driverId,
                truckId,
                employeeId,
                search
        );
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching RTIMaster by ID: {}", id);
        Optional<RTIMasterDto> record = rtiMasterService.getById(id);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RTIMaster not found with ID: " + id);
        }
    }

    @PostMapping
    @PermitAll
    public ResponseEntity<?> create(@Valid @RequestBody RTIMasterDto dto) {
        logger.info("Creating new RTIMaster for company: {}", dto.getCompanyRefId());
        try {
            RTIMasterDto created = rtiMasterService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating RTIMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating RTIMaster: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody RTIMasterDto dto) {
        logger.info("Updating RTIMaster with ID: {}", id);
        try {
            RTIMasterDto updated = rtiMasterService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("RTIMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RTIMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating RTIMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating RTIMaster: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting RTIMaster with ID: {}", id);
        try {
            boolean deleted = rtiMasterService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RTIMaster not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting RTIMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting RTIMaster: " + e.getMessage());
        }
    }

    @GetMapping("/company/{companyRefId}/cnumber/{cNumber}")
    @PermitAll
    public ResponseEntity<?> getByCNumber(@PathVariable Integer companyRefId, @PathVariable Integer cNumber) {
        logger.info("Fetching RTIMaster by CNumber: {} for company: {}", cNumber, companyRefId);
        if (companyRefId == null || cNumber == null) {
            logger.warn("Invalid request: companyRefId or cNumber is null");
            return ResponseEntity.badRequest().body("companyRefId and cNumber must be provided");
        }
        try {
            Optional<RTIMasterDto> record = rtiMasterService.getByCNumber(companyRefId, cNumber);
            if (record.isPresent()) {
                return ResponseEntity.ok(record.get());
            } else {
                logger.warn("RTIMaster not found with CNumber: {} for company: {}", cNumber, companyRefId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(String.format("RTIMaster not found with CNumber: %d for company: %d", cNumber, companyRefId));
            }
        } catch (Exception e) {
            logger.error("Error fetching RTIMaster by CNumber", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching RTIMaster: " + e.getMessage());
        }
    }

    @GetMapping("/company/{companyRefId}/employee/{employeeRefId}")
    @PermitAll
    public ResponseEntity<List<RTIMasterDto>> getByEmployee(@PathVariable Integer companyRefId, @PathVariable Integer employeeRefId) {
        logger.info("Fetching RTIMaster for employee: {}", employeeRefId);
        List<RTIMasterDto> records = rtiMasterService.getByEmployee(companyRefId, employeeRefId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/company/{companyRefId}/agent/{agentMasterRefId}")
    @PermitAll
    public ResponseEntity<List<RTIMasterDto>> getByAgent(@PathVariable Integer companyRefId, @PathVariable Integer agentMasterRefId) {
        logger.info("Fetching RTIMaster for agent: {}", agentMasterRefId);
        List<RTIMasterDto> records = rtiMasterService.getByAgent(companyRefId, agentMasterRefId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/company/{companyRefId}/date-range")
    @PermitAll
    public ResponseEntity<List<RTIMasterDto>> getByDateRange(@PathVariable Integer companyRefId,
            @RequestParam String startDate, @RequestParam String endDate) {
        logger.info("Fetching RTIMaster between dates: {} to {}", startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        List<RTIMasterDto> records = rtiMasterService.getByDateRange(companyRefId, start, end);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/cnumber-display/{cNumberDisplay}")
    @PermitAll
    public ResponseEntity<?> getByCNumberDisplay(@PathVariable String cNumberDisplay) {
        logger.info("Fetching RTIMaster by CNumberDisplay: {}", cNumberDisplay);
        Optional<RTIMasterDto> record = rtiMasterService.getByCNumberDisplay(cNumberDisplay);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RTIMaster not found with CNumberDisplay: " + cNumberDisplay);
        }
    }

    @GetMapping("/company/{companyRefId}/sleeping")
    @PermitAll
    public ResponseEntity<List<RTIMasterDto>> getSleepingRecords(@PathVariable Integer companyRefId) {
        logger.info("Fetching sleeping RTIMaster records for company: {}", companyRefId);
        List<RTIMasterDto> records = rtiMasterService.getSleepingRecords(companyRefId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/company/{companyRefId}/truck/{truckRefId}")
    @PermitAll
    public ResponseEntity<List<RTIMasterDto>> getByTruck(@PathVariable Integer companyRefId, @PathVariable Integer truckRefId) {
        logger.info("Fetching RTIMaster for truck: {}", truckRefId);
        List<RTIMasterDto> records = rtiMasterService.getByTruck(companyRefId, truckRefId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/company/{companyRefId}/count")
    @PermitAll
    public ResponseEntity<?> countByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting RTIMaster records for company: {}", companyRefId);
        long count = rtiMasterService.countByCompanyId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    @GetMapping("/company/{companyRefId}/count/active")
    @PermitAll
    public ResponseEntity<?> countActiveByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting active RTIMaster records for company: {}", companyRefId);
        long count = rtiMasterService.countActiveByCompanyId(companyRefId);
        return ResponseEntity.ok("Active Total: " + count);
    }

    @PostMapping("/{id}/activate")
    @PermitAll
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating RTIMaster with ID: {}", id);
        try {
            RTIMasterDto activated = rtiMasterService.activate(id);
            return ResponseEntity.ok(activated);
        } catch (RuntimeException e) {
            logger.error("RTIMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RTIMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error activating RTIMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error activating RTIMaster: " + e.getMessage());
        }
    }

    // POST /{id}/revise (create/clone) removed - legacy .NET ReviseRTI is read-only. Use GET /{id}/revise to load data for the UI.

    /**
     * Load RTI master and details for revise UI (read-only). Mirrors legacy .NET ReviseRTI which
     * returns master + details joined with SaleOrder and Customer.
     */
    @GetMapping("/{id}/revise")
    @PermitAll
    public ResponseEntity<ApiResponse<RTIMasterDto>> getForRevise(@PathVariable Integer id,
                                                                  @RequestParam(required = false) Integer sourceCNumber,
                                                                  @RequestParam(required = false) Integer companyRefId) {
        logger.info("Loading RTIMaster for revise UI id={} sourceCNumber={}", id, sourceCNumber);
        try {
            RTIMasterDto dto = rtiMasterService.getForRevise(id, sourceCNumber, companyRefId);
            return ResponseEntity.ok(ApiResponse.success(dto, "RTIMaster data for revise"));
        } catch (RuntimeException e) {
            logger.error("RTIMaster not found for revise id={}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error("Error loading RTIMaster for revise", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Error loading RTIMaster for revise: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @PostMapping("/{id}/deactivate")
    @PermitAll
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating RTIMaster with ID: {}", id);
        try {
            RTIMasterDto deactivated = rtiMasterService.deactivate(id);
            return ResponseEntity.ok(deactivated);
        } catch (RuntimeException e) {
            logger.error("RTIMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RTIMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error deactivating RTIMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deactivating RTIMaster: " + e.getMessage());
        }
    }

    @GetMapping("/view-details")
    @PermitAll
    public ResponseEntity<ApiResponse<java.util.List<my.maleva.api.module.rti.dto.RTIViewDto>>> getRtiViewDetails(
            @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            @RequestParam(value = "employeeId", required = false) Integer employeeId) {
        
        logger.info("Fetching RTI View Details for EmployeeId: {}, FromDate: {}, ToDate: {}", employeeId, fromDate, toDate);
        try {
            java.util.List<my.maleva.api.module.rti.dto.RTIViewDto> data = rtiMasterService.getRtiViewDetails(fromDate, toDate, employeeId);
            return ResponseEntity.ok(ApiResponse.success(data, "RTI View Details fetched successfully"));
        } catch (Exception e) {
            logger.error("Error fetching RTI View Details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch RTI View Details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }
    @GetMapping("/company/{companyRefId}/summary")
    public ResponseEntity<ApiResponse<List<RtiSummaryResponseDto>>> getRtiSummary(
            @PathVariable Integer companyRefId,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "driverRefId", required = false) Integer driverRefId) {
        
        List<RtiSummaryResponseDto> data = rtiMasterService.getRtiSummaryByDateRange(companyRefId, fromDate.atStartOfDay(), toDate.atTime(23, 59, 59), driverRefId);
        return ResponseEntity.ok(ApiResponse.success(data, "RTI summary retrieved successfully"));
    }
}