package my.maleva.api.module.salecreditmaster.controller;

import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.salecreditmaster.dto.SaleCreditMasterDto;
import my.maleva.api.module.salecreditmaster.service.SaleCreditMasterService;
import my.maleva.api.module.salecreditmaster.service.SaleCreditQneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SaleCreditMasterController
 * REST Controller for SaleCreditMaster API
 * Provides endpoints for CRUD operations and business logic
 */
@RestController
@RequestMapping("/api/sale-credits")
@PermitAll
public class SaleCreditMasterController {

    private static final Logger logger = LoggerFactory.getLogger(SaleCreditMasterController.class);

    @Autowired
    private SaleCreditMasterService saleCreditMasterService;

    @Autowired
    private SaleCreditQneService saleCreditQneService;

    /**
     * Push credit note to QNE
     * POST /api/sale-credits/{id}/push-qne?companyId=1
     *
     * Legacy synced the CN as a side effect of viewing/printing it
     * (SaleCreditVIEW); here it is this explicit call, still create-once via
     * the empty-QNECode guard. A QNE rejection answers 200 with
     * IsSuccess=false and QNE's own message.
     */
    @PostMapping("/{id}/push-qne")
    @PermitAll
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushToQne(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Pushing credit note ID: {} to QNE for company: {}", id, companyId);
        try {
            if (id == null || id <= 0 || companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid ID or company ID", 400));
            }
            return QnePushResponses.toResponse(saleCreditQneService.push(id, companyId));
        } catch (Exception e) {
            logger.error("Error pushing credit note to QNE", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error pushing to QNE: " + e.getMessage(), 500));
        }
    }

    /**
     * Get all SaleCreditMaster records by company ID
     * GET /api/sale-credits/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditMasterDto>> getAllByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching all SaleCreditMaster records for company: {}", companyRefId);
        List<SaleCreditMasterDto> records = saleCreditMasterService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditMaster records by company and status
     * GET /api/sale-credits/company/{companyRefId}/status/{cStatus}
     */
    @GetMapping("/company/{companyRefId}/status/{cStatus}")
    @PermitAll
    public ResponseEntity<List<SaleCreditMasterDto>> getByCompanyIdAndStatus(
            @PathVariable Integer companyRefId, @PathVariable Integer cStatus) {
        logger.info("Fetching SaleCreditMaster records for company: {} and status: {}", companyRefId, cStatus);
        List<SaleCreditMasterDto> records = saleCreditMasterService.getByCompanyIdAndStatus(companyRefId, cStatus);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditMaster by ID
     * GET /api/sale-credits/{id}
     */
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleCreditMaster by ID: {}", id);
        Optional<SaleCreditMasterDto> record = saleCreditMasterService.getById(id);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditMaster not found with ID: " + id);
    }

    /**
     * Create SaleCreditMaster record
     * POST /api/sale-credits
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<?> create(@Valid @RequestBody SaleCreditMasterDto dto) {
        logger.info("Creating new SaleCreditMaster for company: {}", dto.getCompanyRefId());
        try {
            SaleCreditMasterDto created = saleCreditMasterService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            logger.error("Error creating SaleCreditMaster: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error creating SaleCreditMaster: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating SaleCreditMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating SaleCreditMaster: " + e.getMessage());
        }
    }

    /**
     * Update SaleCreditMaster record
     * PUT /api/sale-credits/{id}
     */
    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleCreditMasterDto dto) {
        logger.info("Updating SaleCreditMaster with ID: {}", id);
        try {
            SaleCreditMasterDto updated = saleCreditMasterService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("SaleCreditMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating SaleCreditMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating SaleCreditMaster: " + e.getMessage());
        }
    }

    /**
     * Delete SaleCreditMaster record
     * DELETE /api/sale-credits/{id}
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleCreditMaster with ID: {}", id);
        boolean deleted = saleCreditMasterService.delete(id);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditMaster not found with ID: " + id);
    }

    /**
     * Get SaleCreditMaster records by customer ID
     * GET /api/sale-credits/customer/{customerRefId}
     */
    @GetMapping("/customer/{customerRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditMasterDto>> getByCustomerRefId(@PathVariable Integer customerRefId) {
        logger.info("Fetching SaleCreditMaster records by customer ID: {}", customerRefId);
        List<SaleCreditMasterDto> records = saleCreditMasterService.getByCustomerRefId(customerRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditMaster records by company and customer
     * GET /api/sale-credits/company/{companyRefId}/customer/{customerRefId}
     */
    @GetMapping("/company/{companyRefId}/customer/{customerRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditMasterDto>> getByCompanyAndCustomer(
            @PathVariable Integer companyRefId, @PathVariable Integer customerRefId) {
        logger.info("Fetching SaleCreditMaster records for company: {} and customer: {}", companyRefId, customerRefId);
        List<SaleCreditMasterDto> records = saleCreditMasterService.getByCompanyAndCustomer(companyRefId, customerRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditMaster records by date range
     * GET /api/sale-credits/company/{companyRefId}/date-range?startDate=&endDate=
     */
    @GetMapping("/company/{companyRefId}/date-range")
    @PermitAll
    public ResponseEntity<?> getByDateRange(
            @PathVariable Integer companyRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        logger.info("Fetching SaleCreditMaster records by date range for company: {}", companyRefId);
        try {
            List<SaleCreditMasterDto> records = saleCreditMasterService.getByDateRange(companyRefId, startDate, endDate);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Error fetching records by date range", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date range format");
        }
    }

    /**
     * Get SaleCreditMaster by reference number
     * GET /api/sale-credits/company/{companyRefId}/ref-number/{refNumber}
     */
    @GetMapping("/company/{companyRefId}/ref-number/{refNumber}")
    @PermitAll
    public ResponseEntity<?> getByRefNumber(@PathVariable Integer companyRefId, @PathVariable String refNumber) {
        logger.info("Fetching SaleCreditMaster by reference number: {}", refNumber);
        Optional<SaleCreditMasterDto> record = saleCreditMasterService.getByRefNumber(companyRefId, refNumber);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditMaster not found with reference number: " + refNumber);
    }

    /**
     * Get SaleCreditMaster by C Number
     * GET /api/sale-credits/company/{companyRefId}/c-number/{cNumber}
     */
    @GetMapping("/company/{companyRefId}/c-number/{cNumber}")
    @PermitAll
    public ResponseEntity<?> getByCNumber(@PathVariable Integer companyRefId, @PathVariable Integer cNumber) {
        logger.info("Fetching SaleCreditMaster by C Number: {}", cNumber);
        Optional<SaleCreditMasterDto> record = saleCreditMasterService.getByCNumber(companyRefId, cNumber);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditMaster not found with C Number: " + cNumber);
    }

    /**
     * Get SaleCreditMaster records by employee ID
     * GET /api/sale-credits/employee/{employeeRefId}
     */
    @GetMapping("/employee/{employeeRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditMasterDto>> getByEmployeeId(@PathVariable Integer employeeRefId) {
        logger.info("Fetching SaleCreditMaster records by employee ID: {}", employeeRefId);
        List<SaleCreditMasterDto> records = saleCreditMasterService.getByEmployeeId(employeeRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditMaster records by company and employee
     * GET /api/sale-credits/company/{companyRefId}/employee/{employeeRefId}
     */
    @GetMapping("/company/{companyRefId}/employee/{employeeRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditMasterDto>> getByCompanyAndEmployee(
            @PathVariable Integer companyRefId, @PathVariable Integer employeeRefId) {
        logger.info("Fetching SaleCreditMaster records for company: {} and employee: {}", companyRefId, employeeRefId);
        List<SaleCreditMasterDto> records = saleCreditMasterService.getByCompanyAndEmployee(companyRefId, employeeRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditMaster records by user ID
     * GET /api/sale-credits/user/{userRefId}
     */
    @GetMapping("/user/{userRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditMasterDto>> getByUserId(@PathVariable Integer userRefId) {
        logger.info("Fetching SaleCreditMaster records by user ID: {}", userRefId);
        List<SaleCreditMasterDto> records = saleCreditMasterService.getByUserId(userRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditMaster records by Sale Master Reference ID
     * GET /api/sale-credits/sale-master/{saleMasterRefId}
     */
    @GetMapping("/sale-master/{saleMasterRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditMasterDto>> getBySaleMasterRefId(@PathVariable Integer saleMasterRefId) {
        logger.info("Fetching SaleCreditMaster records by Sale Master Reference ID: {}", saleMasterRefId);
        List<SaleCreditMasterDto> records = saleCreditMasterService.getBySaleMasterRefId(saleMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Count SaleCreditMaster by company
     * GET /api/sale-credits/count/company/{companyRefId}
     */
    @GetMapping("/count/company/{companyRefId}")
    @PermitAll
    public ResponseEntity<Long> countByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting SaleCreditMaster records for company: {}", companyRefId);
        long count = saleCreditMasterService.countByCompanyId(companyRefId);
        return ResponseEntity.ok(count);
    }

    /**
     * Count SaleCreditMaster by company and status
     * GET /api/sale-credits/count/company/{companyRefId}/status/{cStatus}
     */
    @GetMapping("/count/company/{companyRefId}/status/{cStatus}")
    @PermitAll
    public ResponseEntity<Long> countByCompanyIdAndStatus(
            @PathVariable Integer companyRefId, @PathVariable Integer cStatus) {
        logger.info("Counting SaleCreditMaster records for company: {} and status: {}", companyRefId, cStatus);
        long count = saleCreditMasterService.countByCompanyIdAndStatus(companyRefId, cStatus);
        return ResponseEntity.ok(count);
    }

    /**
     * Count SaleCreditMaster by customer
     * GET /api/sale-credits/count/customer/{customerRefId}
     */
    @GetMapping("/count/customer/{customerRefId}")
    @PermitAll
    public ResponseEntity<Long> countByCustomerRefId(@PathVariable Integer customerRefId) {
        logger.info("Counting SaleCreditMaster records by customer: {}", customerRefId);
        long count = saleCreditMasterService.countByCustomerRefId(customerRefId);
        return ResponseEntity.ok(count);
    }

    /**
     * Change SaleCreditMaster status
     * PATCH /api/sale-credits/{id}/status/{newStatus}
     */
    @PatchMapping("/{id}/status/{newStatus}")
    @PermitAll
    public ResponseEntity<?> changeStatus(@PathVariable Integer id, @PathVariable Integer newStatus) {
        logger.info("Changing status for SaleCreditMaster with ID: {} to {}", id, newStatus);
        try {
            SaleCreditMasterDto updated = saleCreditMasterService.changeStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("SaleCreditMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error changing status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error changing status: " + e.getMessage());
        }
    }

    /**
     * Activate SaleCreditMaster
     * POST /api/sale-credits/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PermitAll
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating SaleCreditMaster with ID: {}", id);
        try {
            SaleCreditMasterDto updated = saleCreditMasterService.activate(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("SaleCreditMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error activating SaleCreditMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error activating SaleCreditMaster: " + e.getMessage());
        }
    }

    /**
     * Deactivate SaleCreditMaster
     * POST /api/sale-credits/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PermitAll
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating SaleCreditMaster with ID: {}", id);
        try {
            SaleCreditMasterDto updated = saleCreditMasterService.deactivate(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("SaleCreditMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error deactivating SaleCreditMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deactivating SaleCreditMaster: " + e.getMessage());
        }
    }

    /**
     * Get SaleCreditMaster records by date range and status
     * GET /api/sale-credits/company/{companyRefId}/date-range-status?startDate=&endDate=&cStatus=
     */
    @GetMapping("/company/{companyRefId}/date-range-status")
    @PermitAll
    public ResponseEntity<?> getByDateAndStatus(
            @PathVariable Integer companyRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam Integer cStatus) {
        logger.info("Fetching SaleCreditMaster records by date and status for company: {}", companyRefId);
        try {
            List<SaleCreditMasterDto> records = saleCreditMasterService.getByDateAndStatus(companyRefId, startDate, endDate, cStatus);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            logger.error("Error fetching records by date and status", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid parameters");
        }
    }
}


