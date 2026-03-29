package my.maleva.api.module.prealert.controller;

import my.maleva.api.module.prealert.dto.PreAlertMasterDto;
import my.maleva.api.module.prealert.service.PreAlertMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PreAlertMaster REST Controller
 * Handles all RESTful API endpoints for PreAlertMaster operations
 *
 * Base URL: /api/pre-alert-masters
 */
@RestController
@RequestMapping("/api/pre-alert-masters")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PreAlertMasterController {

    private static final Logger logger = LoggerFactory.getLogger(PreAlertMasterController.class);

    @Autowired
    private PreAlertMasterService preAlertMasterService;

    /**
     * Get all PreAlertMaster records by company ID
     *
     * @param companyRefId Company reference ID
     * @return List of PreAlertMaster records
     */
    @GetMapping("/company/{companyRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getAllByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching all PreAlertMaster records for company: {}", companyRefId);
        List<PreAlertMasterDto> records = preAlertMasterService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get active PreAlertMaster records by company ID
     *
     * @param companyRefId Company reference ID
     * @return List of active PreAlertMaster records
     */
    @GetMapping("/company/{companyRefId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getActiveByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching active PreAlertMaster records for company: {}", companyRefId);
        List<PreAlertMasterDto> records = preAlertMasterService.getActiveByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlertMaster by ID
     *
     * @param id PreAlertMaster ID
     * @return PreAlertMaster details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching PreAlertMaster by ID: {}", id);
        Optional<PreAlertMasterDto> record = preAlertMasterService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PreAlertMaster not found with ID: " + id);
        }
    }

    /**
     * Create new PreAlertMaster record
     *
     * @param dto PreAlertMaster DTO
     * @return Created PreAlertMaster record
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody PreAlertMasterDto dto) {
        logger.info("Creating new PreAlertMaster for company: {}", dto.getCompanyRefId());

        try {
            PreAlertMasterDto created = preAlertMasterService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating PreAlertMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating PreAlertMaster: " + e.getMessage());
        }
    }

    /**
     * Update PreAlertMaster record
     *
     * @param id PreAlertMaster ID
     * @param dto Updated PreAlertMaster DTO
     * @return Updated PreAlertMaster record
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody PreAlertMasterDto dto) {
        logger.info("Updating PreAlertMaster with ID: {}", id);

        try {
            PreAlertMasterDto updated = preAlertMasterService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating PreAlertMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating PreAlertMaster: " + e.getMessage());
        }
    }

    /**
     * Delete PreAlertMaster record
     *
     * @param id PreAlertMaster ID
     * @return Success/Failure message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting PreAlertMaster with ID: {}", id);

        boolean deleted = preAlertMasterService.delete(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PreAlertMaster not found with ID: " + id);
        }
    }

    /**
     * Get PreAlertMaster records by customer ID
     *
     * @param customerMasterRefId Customer reference ID
     * @return List of PreAlertMaster records
     */
    @GetMapping("/customer/{customerMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByCustomerId(
            @PathVariable Integer customerMasterRefId) {
        logger.info("Fetching PreAlertMaster records for customer: {}", customerMasterRefId);
        List<PreAlertMasterDto> records = preAlertMasterService.getByCustomerId(customerMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlertMaster records by job type ID
     *
     * @param jobTypeMasterRefId Job type reference ID
     * @return List of PreAlertMaster records
     */
    @GetMapping("/job-type/{jobTypeMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByJobTypeId(
            @PathVariable Integer jobTypeMasterRefId) {
        logger.info("Fetching PreAlertMaster records for job type: {}", jobTypeMasterRefId);
        List<PreAlertMasterDto> records = preAlertMasterService.getByJobTypeId(jobTypeMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlertMaster records by port
     *
     * @param port Port name
     * @return List of PreAlertMaster records
     */
    @GetMapping("/port/{port}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByPort(@PathVariable String port) {
        logger.info("Fetching PreAlertMaster records for port: {}", port);
        List<PreAlertMasterDto> records = preAlertMasterService.getByPort(port);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlertMaster records by vessel name
     *
     * @param vessel Vessel name
     * @return List of PreAlertMaster records
     */
    @GetMapping("/vessel/{vessel}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByVessel(@PathVariable String vessel) {
        logger.info("Fetching PreAlertMaster records for vessel: {}", vessel);
        List<PreAlertMasterDto> records = preAlertMasterService.getByVessel(vessel);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlertMaster records within date range
     *
     * @param companyId Company reference ID
     * @param fromDate From date (format: yyyy-MM-dd)
     * @param toDate To date (format: yyyy-MM-dd)
     * @return List of PreAlertMaster records
     */
    @GetMapping("/company/{companyId}/date-range")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByDateRange(
            @PathVariable Integer companyId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate) {
        logger.info("Fetching PreAlertMaster records for date range: {} to {}", fromDate, toDate);
        List<PreAlertMasterDto> records = preAlertMasterService.getByDateRange(companyId, fromDate, toDate);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlertMaster by CNumber
     *
     * @param cNumber Sequence number
     * @param companyRefId Company reference ID
     * @return PreAlertMaster record
     */
    @GetMapping("/cnumber/{cNumber}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getByCNumber(
            @PathVariable Integer cNumber,
            @RequestParam Integer companyRefId) {
        logger.info("Fetching PreAlertMaster by CNumber: {}", cNumber);
        Optional<PreAlertMasterDto> record = preAlertMasterService.getByCNumber(cNumber, companyRefId);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PreAlertMaster not found with CNumber: " + cNumber);
        }
    }

    /**
     * Get PreAlertMaster by CNumberDisplay
     *
     * @param cNumberDisplay Display number (e.g., PA0001/2026)
     * @return PreAlertMaster record
     */
    @GetMapping("/cnumber-display/{cNumberDisplay}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getByCNumberDisplay(@PathVariable String cNumberDisplay) {
        logger.info("Fetching PreAlertMaster by CNumberDisplay: {}", cNumberDisplay);
        Optional<PreAlertMasterDto> record = preAlertMasterService.getByCNumberDisplay(cNumberDisplay);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PreAlertMaster not found with CNumberDisplay: " + cNumberDisplay);
        }
    }

    /**
     * Activate PreAlertMaster record
     *
     * @param id PreAlertMaster ID
     * @return Activated PreAlertMaster record
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating PreAlertMaster with ID: {}", id);

        try {
            PreAlertMasterDto activated = preAlertMasterService.activate(id);
            return ResponseEntity.ok(activated);
        } catch (Exception e) {
            logger.error("Error activating PreAlertMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating PreAlertMaster: " + e.getMessage());
        }
    }

    /**
     * Deactivate PreAlertMaster record
     *
     * @param id PreAlertMaster ID
     * @return Deactivated PreAlertMaster record
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating PreAlertMaster with ID: {}", id);

        try {
            PreAlertMasterDto deactivated = preAlertMasterService.deactivate(id);
            return ResponseEntity.ok(deactivated);
        } catch (Exception e) {
            logger.error("Error deactivating PreAlertMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deactivating PreAlertMaster: " + e.getMessage());
        }
    }

    /**
     * Execute SP_PreAlert stored procedure for bulk operations
     *
     * @param masterJson JSON containing PreAlertMaster and PreAlert details
     * @param companyId Company reference ID
     * @return Success/Failure response
     */
    @PostMapping("/bulk-import")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> executeBulkImport(
            @RequestParam String masterJson,
            @RequestParam Integer companyId) {
        logger.info("Executing bulk import for PreAlert with company ID: {}", companyId);

        try {
            preAlertMasterService.executePreAlertStoredProcedure(masterJson, companyId);
            return ResponseEntity.ok().body("Bulk import executed successfully");
        } catch (Exception e) {
            logger.error("Error executing bulk import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing bulk import: " + e.getMessage());
        }
    }

    /**
     * Get count of active PreAlertMaster records
     *
     * @param companyRefId Company reference ID
     * @return Count of active records
     */
    @GetMapping("/company/{companyRefId}/count-active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<Long> countActiveRecords(@PathVariable Integer companyRefId) {
        logger.info("Counting active PreAlertMaster records for company: {}", companyRefId);
        Long count = preAlertMasterService.countActiveRecords(companyRefId);
        return ResponseEntity.ok(count);
    }
}

