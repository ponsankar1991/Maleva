package my.maleva.api.controller;

import my.maleva.api.dto.TollEntryDto;
import my.maleva.api.dto.TollEntryDetailsDto;
import my.maleva.api.service.TollEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TollEntryController - REST Controller for TollEntry API
 */
@RestController
@RequestMapping("/api/toll-entries")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
public class TollEntryController {

    private static final Logger logger = LoggerFactory.getLogger(TollEntryController.class);

    @Autowired
    private TollEntryService service;

    /**
     * Get all TollEntry records by company ID
     * GET /api/toll-entries/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<TollEntryDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching TollEntry for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get active TollEntry records by company
     * GET /api/toll-entries/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<List<TollEntryDto>> getActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active TollEntry for company: {}", companyRefId);
        return ResponseEntity.ok(service.getActiveByCompanyRefId(companyRefId));
    }

    /**
     * Get TollEntry by C Number
     * GET /api/toll-entries/c-number/{cNumber}/company/{companyRefId}
     */
    @GetMapping("/c-number/{cNumber}/company/{companyRefId}")
    public ResponseEntity<?> getByCNumber(@PathVariable Integer cNumber, @PathVariable Integer companyRefId) {
        logger.info("Fetching TollEntry by C Number: {} for company: {}", cNumber, companyRefId);
        Optional<TollEntryDto> record = service.getByCNumber(cNumber, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get TollEntry records by user ID
     * GET /api/toll-entries/user/{userRefId}
     */
    @GetMapping("/user/{userRefId}")
    public ResponseEntity<List<TollEntryDto>> getByUserRefId(@PathVariable Integer userRefId) {
        logger.info("Fetching TollEntry for user: {}", userRefId);
        return ResponseEntity.ok(service.getByUserRefId(userRefId));
    }

    /**
     * Get TollEntry records by employee ID
     * GET /api/toll-entries/employee/{employeeRefId}
     */
    @GetMapping("/employee/{employeeRefId}")
    public ResponseEntity<List<TollEntryDto>> getByEmployeeRefId(@PathVariable Integer employeeRefId) {
        logger.info("Fetching TollEntry for employee: {}", employeeRefId);
        return ResponseEntity.ok(service.getByEmployeeRefId(employeeRefId));
    }

    /**
     * Get TollEntry records by truck ID
     * GET /api/toll-entries/truck/{truckRefid}
     */
    @GetMapping("/truck/{truckRefid}")
    public ResponseEntity<List<TollEntryDto>> getByTruckRefid(@PathVariable Integer truckRefid) {
        logger.info("Fetching TollEntry for truck: {}", truckRefid);
        return ResponseEntity.ok(service.getByTruckRefid(truckRefid));
    }

    /**
     * Get TollEntry by ID
     * GET /api/toll-entries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching TollEntry by ID: {}", id);
        Optional<TollEntryDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new TollEntry
     * POST /api/toll-entries
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TollEntryDto dto) {
        logger.info("Creating new TollEntry");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Process TollEntry (SP_TollEntry logic - INSERT or UPDATE with details)
     * POST /api/toll-entries/process?companyId=1
     */
    @PostMapping("/process")
    public ResponseEntity<?> processTollEntry(
            @Valid @RequestBody TollEntryRequest request,
            @RequestParam Integer companyId) {
        logger.info("Processing TollEntry with SP_TollEntry logic for company: {}", companyId);
        try {
            TollEntryDto result = service.processTollEntry(
                    request.getTollEntry(),
                    request.getDetails(),
                    companyId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update TollEntry
     * PUT /api/toll-entries/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody TollEntryDto dto) {
        logger.info("Updating TollEntry with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete TollEntry
     * DELETE /api/toll-entries/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting TollEntry with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Activate TollEntry
     * PUT /api/toll-entries/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateTollEntry(@PathVariable Integer id) {
        logger.info("Activating TollEntry with ID: {}", id);
        try {
            return ResponseEntity.ok(service.activateTollEntry(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Deactivate TollEntry
     * PUT /api/toll-entries/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateTollEntry(@PathVariable Integer id) {
        logger.info("Deactivating TollEntry with ID: {}", id);
        try {
            return ResponseEntity.ok(service.deactivateTollEntry(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Count TollEntry records by company ID
     * GET /api/toll-entries/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting TollEntry for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active TollEntry records by company
     * GET /api/toll-entries/company/{companyRefId}/active/count
     */
    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<?> countActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting active TollEntry for company: {}", companyRefId);
        long count = service.countActiveByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Inner class for TollEntry request with details
     */
    public static class TollEntryRequest {
        private TollEntryDto tollEntry;
        private List<TollEntryDetailsDto> details;

        public TollEntryDto getTollEntry() {
            return tollEntry;
        }

        public void setTollEntry(TollEntryDto tollEntry) {
            this.tollEntry = tollEntry;
        }

        public List<TollEntryDetailsDto> getDetails() {
            return details;
        }

        public void setDetails(List<TollEntryDetailsDto> details) {
            this.details = details;
        }
    }
}

