package my.maleva.api.module.prealert.controller;

import my.maleva.api.module.prealert.dto.PreAlertDto;
import my.maleva.api.module.prealert.dto.PreAlertMasterDto;
import my.maleva.api.module.prealert.service.PreAlertMasterService;
import my.maleva.api.module.prealert.service.PreAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PreAlertMaster REST Controller
 * Handles the authoritative PreAlert API surface using the master record as the entry point.
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

    @Autowired
    private PreAlertService preAlertService;

    /**
     * Get all PreAlertMaster records by company ID.
     */
    @GetMapping("/company/{companyRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getAllByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching all PreAlertMaster records for company: {}", companyRefId);
        return ResponseEntity.ok(preAlertMasterService.getAllByCompanyId(companyRefId));
    }

    /**
     * Get active PreAlertMaster records by company ID.
     */
    @GetMapping("/company/{companyRefId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getActiveByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active PreAlertMaster records for company: {}", companyRefId);
        return ResponseEntity.ok(preAlertMasterService.getActiveByCompanyId(companyRefId));
    }

    /**
     * Get PreAlertMaster by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching PreAlertMaster by ID: {}", id);
        Optional<PreAlertMasterDto> record = preAlertMasterService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("PreAlertMaster not found with ID: " + id);
    }

    /**
     * Get PreAlertMaster records by customer ID.
     */
    @GetMapping("/customer/{customerMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByCustomerId(@PathVariable Integer customerMasterRefId) {
        logger.info("Fetching PreAlertMaster records for customer: {}", customerMasterRefId);
        return ResponseEntity.ok(preAlertMasterService.getByCustomerId(customerMasterRefId));
    }

    /**
     * Get PreAlertMaster records by job type ID.
     */
    @GetMapping("/job-type/{jobTypeMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByJobTypeId(@PathVariable Integer jobTypeMasterRefId) {
        logger.info("Fetching PreAlertMaster records for job type: {}", jobTypeMasterRefId);
        return ResponseEntity.ok(preAlertMasterService.getByJobTypeId(jobTypeMasterRefId));
    }

    /**
     * Get PreAlertMaster records by port.
     */
    @GetMapping("/port/{port}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByPort(@PathVariable String port) {
        logger.info("Fetching PreAlertMaster records for port: {}", port);
        return ResponseEntity.ok(preAlertMasterService.getByPort(port));
    }

    /**
     * Get PreAlertMaster records by vessel name.
     */
    @GetMapping("/vessel/{vessel}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByVessel(@PathVariable String vessel) {
        logger.info("Fetching PreAlertMaster records for vessel: {}", vessel);
        return ResponseEntity.ok(preAlertMasterService.getByVessel(vessel));
    }

    /**
     * Get PreAlertMaster records within date range.
     */
    @GetMapping("/company/{companyId}/date-range")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertMasterDto>> getByDateRange(
            @PathVariable Integer companyId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate) {
        logger.info("Fetching PreAlertMaster records for date range: {} to {}", fromDate, toDate);
        return ResponseEntity.ok(preAlertMasterService.getByDateRange(companyId, fromDate, toDate));
    }

    /**
     * Get PreAlertMaster by CNumber.
     */
    @GetMapping("/cnumber/{cNumber}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getByCNumber(@PathVariable Integer cNumber, @RequestParam Integer companyRefId) {
        logger.info("Fetching PreAlertMaster by CNumber: {}", cNumber);
        Optional<PreAlertMasterDto> record = preAlertMasterService.getByCNumber(cNumber, companyRefId);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("PreAlertMaster not found with CNumber: " + cNumber);
    }

    /**
     * Get PreAlertMaster by CNumberDisplay.
     */
    @GetMapping("/cnumber-display/{cNumberDisplay}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getByCNumberDisplay(@PathVariable String cNumberDisplay) {
        logger.info("Fetching PreAlertMaster by CNumberDisplay: {}", cNumberDisplay);
        Optional<PreAlertMasterDto> record = preAlertMasterService.getByCNumberDisplay(cNumberDisplay);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("PreAlertMaster not found with CNumberDisplay: " + cNumberDisplay);
    }

    /**
     * Get child PreAlert detail records for a master record.
     */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getDetailsByMasterId(@PathVariable Integer id) {
        logger.info("Fetching PreAlert detail records for master ID: {}", id);
        return ResponseEntity.ok(preAlertService.getByPreAlertMasterId(id));
    }

    /**
     * Get child PreAlert detail count for a master record.
     */
    @GetMapping("/{id}/details/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<Long> countDetailsByMasterId(@PathVariable Integer id) {
        logger.info("Counting PreAlert detail records for master ID: {}", id);
        return ResponseEntity.ok(preAlertService.countByPreAlertMasterId(id));
    }

    /**
     * Delete PreAlertMaster record.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting PreAlertMaster with ID: {}", id);

        boolean deleted = preAlertMasterService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("PreAlertMaster not found with ID: " + id);
    }

    /**
     * Activate PreAlertMaster record.
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating PreAlertMaster with ID: {}", id);

        try {
            return ResponseEntity.ok(preAlertMasterService.activate(id));
        } catch (Exception e) {
            logger.error("Error activating PreAlertMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating PreAlertMaster: " + e.getMessage());
        }
    }

    /**
     * Deactivate PreAlertMaster record.
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating PreAlertMaster with ID: {}", id);

        try {
            return ResponseEntity.ok(preAlertMasterService.deactivate(id));
        } catch (Exception e) {
            logger.error("Error deactivating PreAlertMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deactivating PreAlertMaster: " + e.getMessage());
        }
    }

    /**
     * Execute SP_PreAlert stored procedure for authoritative write operations.
     */
    @PostMapping("/bulk-import")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> executeBulkImport(
            @RequestParam String masterJson,
            @RequestParam Integer companyId) {
        logger.info("Executing bulk import for PreAlert with company ID: {}", companyId);

        try {
            preAlertMasterService.executePreAlertStoredProcedure(masterJson, companyId);
            return ResponseEntity.ok("Bulk import executed successfully");
        } catch (Exception e) {
            logger.error("Error executing bulk import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing bulk import: " + e.getMessage());
        }
    }

    /**
     * Get count of active PreAlertMaster records.
     */
    @GetMapping("/company/{companyRefId}/count-active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<Long> countActiveRecords(@PathVariable Integer companyRefId) {
        logger.info("Counting active PreAlertMaster records for company: {}", companyRefId);
        return ResponseEntity.ok(preAlertMasterService.countActiveRecords(companyRefId));
    }
}
