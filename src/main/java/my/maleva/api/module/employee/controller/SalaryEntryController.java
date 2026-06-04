package my.maleva.api.module.employee.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.employee.dto.SalaryEntryDto;
import my.maleva.api.module.employee.service.SalaryEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

/**
 * SalaryEntry REST Controller
 * Handles all RESTful API endpoints for SalaryEntry operations
 * Base URL: /api/salary-entries
 */
@RestController
@RequestMapping("/api/salary-entries")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SalaryEntryController {

    private static final Logger logger = LoggerFactory.getLogger(SalaryEntryController.class);

    @Autowired
    private SalaryEntryService salaryEntryService;

    /**
     * Get all SalaryEntry records by company ID
     * GET /api/salary-entries/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    @PermitAll
    public ResponseEntity<List<SalaryEntryDto>> getAllByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching all SalaryEntry records for company: {}", companyRefId);
        List<SalaryEntryDto> records = salaryEntryService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get active SalaryEntry records by company ID
     * GET /api/salary-entries/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    @PermitAll
    public ResponseEntity<List<SalaryEntryDto>> getActiveByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active SalaryEntry records for company: {}", companyRefId);
        List<SalaryEntryDto> records = salaryEntryService.getActiveByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SalaryEntry by ID
     * GET /api/salary-entries/{id}
     */
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SalaryEntry by ID: {}", id);
        Optional<SalaryEntryDto> record = salaryEntryService.getById(id);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SalaryEntry not found with ID: " + id);
        }
    }

    /**
     * Create new SalaryEntry record
     * POST /api/salary-entries
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<?> create(@Valid @RequestBody SalaryEntryDto dto) {
        logger.info("Creating new SalaryEntry for company: {}", dto.getCompanyRefId());
        try {
            SalaryEntryDto created = salaryEntryService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating SalaryEntry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating SalaryEntry: " + e.getMessage());
        }
    }

    /**
     * Update SalaryEntry record
     * PUT /api/salary-entries/{id}
     */
    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SalaryEntryDto dto) {
        logger.info("Updating SalaryEntry with ID: {}", id);
        try {
            SalaryEntryDto updated = salaryEntryService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("SalaryEntry not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SalaryEntry not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating SalaryEntry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating SalaryEntry: " + e.getMessage());
        }
    }

    /**
     * Delete SalaryEntry record
     * DELETE /api/salary-entries/{id}
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SalaryEntry with ID: {}", id);
        try {
            boolean deleted = salaryEntryService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SalaryEntry not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting SalaryEntry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting SalaryEntry: " + e.getMessage());
        }
    }

    /**
     * Get SalaryEntry records by employee ID
     * GET /api/salary-entries/employee/{employeeRefId}
     */
    @GetMapping("/employee/{employeeRefId}")
    @PermitAll
    public ResponseEntity<List<SalaryEntryDto>> getByEmployeeId(@PathVariable Integer employeeRefId) {
        logger.info("Fetching SalaryEntry records for employee: {}", employeeRefId);
        List<SalaryEntryDto> records = salaryEntryService.getByEmployeeId(employeeRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SalaryEntry records by company and employee ID
     * GET /api/salary-entries/company/{companyRefId}/employee/{employeeRefId}
     */
    @GetMapping("/company/{companyRefId}/employee/{employeeRefId}")
    @PermitAll
    public ResponseEntity<List<SalaryEntryDto>> getByCompanyAndEmployee(
            @PathVariable Integer companyRefId, @PathVariable Integer employeeRefId) {
        logger.info("Fetching SalaryEntry records for company: {} and employee: {}", companyRefId, employeeRefId);
        List<SalaryEntryDto> records = salaryEntryService.getByCompanyAndEmployee(companyRefId, employeeRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SalaryEntry by reference number
     * GET /api/salary-entries/company/{companyRefId}/ref/{refNumber}
     */
    @GetMapping("/company/{companyRefId}/ref/{refNumber}")
    @PermitAll
    public ResponseEntity<?> getByRefNumber(@PathVariable Integer companyRefId, @PathVariable String refNumber) {
        logger.info("Fetching SalaryEntry by reference number: {}", refNumber);
        Optional<SalaryEntryDto> record = salaryEntryService.getByRefNumber(companyRefId, refNumber);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SalaryEntry not found with reference number: " + refNumber);
        }
    }

    /**
     * Get SalaryEntry by C Number
     * GET /api/salary-entries/company/{companyRefId}/cnumber/{cNumber}
     */
    @GetMapping("/company/{companyRefId}/cnumber/{cNumber}")
    @PermitAll
    public ResponseEntity<?> getByCNumber(@PathVariable Integer companyRefId, @PathVariable Integer cNumber) {
        logger.info("Fetching SalaryEntry by C Number: {}", cNumber);
        Optional<SalaryEntryDto> record = salaryEntryService.getByCNumber(companyRefId, cNumber);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SalaryEntry not found with C Number: " + cNumber);
        }
    }

    /**
     * Get SalaryEntry records by bank ID
     * GET /api/salary-entries/bank/{bankRefId}
     */
    @GetMapping("/bank/{bankRefId}")
    @PermitAll
    public ResponseEntity<List<SalaryEntryDto>> getByBankId(@PathVariable Integer bankRefId) {
        logger.info("Fetching SalaryEntry records for bank: {}", bankRefId);
        List<SalaryEntryDto> records = salaryEntryService.getByBankId(bankRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SalaryEntry records by PV Status
     * GET /api/salary-entries/company/{companyRefId}/status/{pvStatus}
     */
    @GetMapping("/company/{companyRefId}/status/{pvStatus}")
    @PermitAll
    public ResponseEntity<List<SalaryEntryDto>> getByPvStatus(@PathVariable Integer companyRefId, @PathVariable Integer pvStatus) {
        logger.info("Fetching SalaryEntry records with PV Status: {}", pvStatus);
        List<SalaryEntryDto> records = salaryEntryService.getByPvStatus(companyRefId, pvStatus);
        return ResponseEntity.ok(records);
    }

    /**
     * Count SalaryEntry by company
     * GET /api/salary-entries/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    @PermitAll
    public ResponseEntity<?> countByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting SalaryEntry records for company: {}", companyRefId);
        long count = salaryEntryService.countByCompanyId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active SalaryEntry by company
     * GET /api/salary-entries/company/{companyRefId}/count/active
     */
    @GetMapping("/company/{companyRefId}/count/active")
    @PermitAll
    public ResponseEntity<?> countActiveByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting active SalaryEntry records for company: {}", companyRefId);
        long count = salaryEntryService.countActiveByCompanyId(companyRefId);
        return ResponseEntity.ok("Active Total: " + count);
    }

    /**
     * Count SalaryEntry by employee
     * GET /api/salary-entries/employee/{employeeRefId}/count
     */
    @GetMapping("/employee/{employeeRefId}/count")
    @PermitAll
    public ResponseEntity<?> countByEmployeeId(@PathVariable Integer employeeRefId) {
        logger.info("Counting SalaryEntry records for employee: {}", employeeRefId);
        long count = salaryEntryService.countByEmployeeId(employeeRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Activate SalaryEntry record
     * POST /api/salary-entries/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PermitAll
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating SalaryEntry with ID: {}", id);
        try {
            SalaryEntryDto activated = salaryEntryService.activate(id);
            return ResponseEntity.ok(activated);
        } catch (RuntimeException e) {
            logger.error("SalaryEntry not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SalaryEntry not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error activating SalaryEntry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error activating SalaryEntry: " + e.getMessage());
        }
    }

    /**
     * Deactivate SalaryEntry record
     * POST /api/salary-entries/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PermitAll
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating SalaryEntry with ID: {}", id);
        try {
            SalaryEntryDto deactivated = salaryEntryService.deactivate(id);
            return ResponseEntity.ok(deactivated);
        } catch (RuntimeException e) {
            logger.error("SalaryEntry not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SalaryEntry not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error deactivating SalaryEntry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deactivating SalaryEntry: " + e.getMessage());
        }
    }

    /**
     * Update PV Status for SalaryEntry
     * PATCH /api/salary-entries/{id}/pvstatus/{pvStatus}
     */
    @PatchMapping("/{id}/pvstatus/{pvStatus}")
    @PermitAll
    public ResponseEntity<?> updatePvStatus(@PathVariable Integer id, @PathVariable Integer pvStatus) {
        logger.info("Updating PV Status for SalaryEntry with ID: {}", id);
        try {
            SalaryEntryDto updated = salaryEntryService.updatePvStatus(id, pvStatus);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("SalaryEntry not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SalaryEntry not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating PV Status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating PV Status: " + e.getMessage());
        }
    }
}



