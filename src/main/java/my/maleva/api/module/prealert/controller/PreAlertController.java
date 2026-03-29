package my.maleva.api.module.prealert.controller;

import my.maleva.api.module.prealert.dto.PreAlertDto;
import my.maleva.api.module.prealert.service.PreAlertService;
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
 * PreAlert REST Controller
 * Handles all RESTful API endpoints for PreAlert detail records
 *
 * Base URL: /api/pre-alerts
 */
@RestController
@RequestMapping("/api/pre-alerts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PreAlertController {

    private static final Logger logger = LoggerFactory.getLogger(PreAlertController.class);

    @Autowired
    private PreAlertService preAlertService;

    /**
     * Get all PreAlert records by company ID
     *
     * @param companyRefId Company reference ID
     * @return List of PreAlert records
     */
    @GetMapping("/company/{companyRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getAllByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching all PreAlert records for company: {}", companyRefId);
        List<PreAlertDto> records = preAlertService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get active PreAlert records by company ID
     *
     * @param companyRefId Company reference ID
     * @return List of active PreAlert records
     */
    @GetMapping("/company/{companyRefId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getActiveByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching active PreAlert records for company: {}", companyRefId);
        List<PreAlertDto> records = preAlertService.getActiveByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlert by ID
     *
     * @param id PreAlert ID
     * @return PreAlert details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching PreAlert by ID: {}", id);
        Optional<PreAlertDto> record = preAlertService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PreAlert not found with ID: " + id);
        }
    }

    /**
     * Create new PreAlert record
     *
     * @param dto PreAlert DTO
     * @return Created PreAlert record
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody PreAlertDto dto) {
        logger.info("Creating new PreAlert for company: {}", dto.getCompanyRefId());

        try {
            PreAlertDto created = preAlertService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating PreAlert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating PreAlert: " + e.getMessage());
        }
    }

    /**
     * Update PreAlert record
     *
     * @param id PreAlert ID
     * @param dto Updated PreAlert DTO
     * @return Updated PreAlert record
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody PreAlertDto dto) {
        logger.info("Updating PreAlert with ID: {}", id);

        try {
            PreAlertDto updated = preAlertService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating PreAlert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating PreAlert: " + e.getMessage());
        }
    }

    /**
     * Delete PreAlert record
     *
     * @param id PreAlert ID
     * @return Success/Failure message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting PreAlert with ID: {}", id);

        boolean deleted = preAlertService.delete(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PreAlert not found with ID: " + id);
        }
    }

    /**
     * Get all PreAlert records by PreAlertMaster ID
     *
     * @param preAlertMasterRefId PreAlertMaster reference ID
     * @return List of PreAlert records
     */
    @GetMapping("/master/{preAlertMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getByPreAlertMasterId(
            @PathVariable Integer preAlertMasterRefId) {
        logger.info("Fetching PreAlert records for master: {}", preAlertMasterRefId);
        List<PreAlertDto> records = preAlertService.getByPreAlertMasterId(preAlertMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlert records by customer ID
     *
     * @param customerMasterRefId Customer reference ID
     * @return List of PreAlert records
     */
    @GetMapping("/customer/{customerMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getByCustomerId(
            @PathVariable Integer customerMasterRefId) {
        logger.info("Fetching PreAlert records for customer: {}", customerMasterRefId);
        List<PreAlertDto> records = preAlertService.getByCustomerId(customerMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlert records by employee ID
     *
     * @param employeeMasterRefId Employee reference ID
     * @return List of PreAlert records
     */
    @GetMapping("/employee/{employeeMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getByEmployeeId(
            @PathVariable Integer employeeMasterRefId) {
        logger.info("Fetching PreAlert records for employee: {}", employeeMasterRefId);
        List<PreAlertDto> records = preAlertService.getByEmployeeId(employeeMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlert records by job type ID
     *
     * @param jobTypeMasterRefId Job type reference ID
     * @return List of PreAlert records
     */
    @GetMapping("/job-type/{jobTypeMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getByJobTypeId(
            @PathVariable Integer jobTypeMasterRefId) {
        logger.info("Fetching PreAlert records for job type: {}", jobTypeMasterRefId);
        List<PreAlertDto> records = preAlertService.getByJobTypeId(jobTypeMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlert records by job status ID
     *
     * @param jobStatusMasterRefId Job status reference ID
     * @return List of PreAlert records
     */
    @GetMapping("/job-status/{jobStatusMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getByJobStatusId(
            @PathVariable Integer jobStatusMasterRefId) {
        logger.info("Fetching PreAlert records for job status: {}", jobStatusMasterRefId);
        List<PreAlertDto> records = preAlertService.getByJobStatusId(jobStatusMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlert records by boarding officer ID
     *
     * @param boardingOfficerRefId Boarding officer reference ID
     * @return List of PreAlert records
     */
    @GetMapping("/boarding-officer/{boardingOfficerRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getByBoardingOfficerId(
            @PathVariable Integer boardingOfficerRefId) {
        logger.info("Fetching PreAlert records for boarding officer: {}", boardingOfficerRefId);
        List<PreAlertDto> records = preAlertService.getByBoardingOfficerId(boardingOfficerRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlert records by vessel name
     *
     * @param vessel Vessel name
     * @return List of PreAlert records
     */
    @GetMapping("/vessel/{vessel}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getByVessel(@PathVariable String vessel) {
        logger.info("Fetching PreAlert records for vessel: {}", vessel);
        List<PreAlertDto> records = preAlertService.getByVessel(vessel);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlert records by port
     *
     * @param port Port name
     * @return List of PreAlert records
     */
    @GetMapping("/port/{port}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getByPort(@PathVariable String port) {
        logger.info("Fetching PreAlert records for port: {}", port);
        List<PreAlertDto> records = preAlertService.getByPort(port);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PreAlert records by job number
     *
     * @param jobNo Job number
     * @return List of PreAlert records
     */
    @GetMapping("/job-no/{jobNo}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PreAlertDto>> getByJobNo(@PathVariable String jobNo) {
        logger.info("Fetching PreAlert records for job number: {}", jobNo);
        List<PreAlertDto> records = preAlertService.getByJobNo(jobNo);
        return ResponseEntity.ok(records);
    }

    /**
     * Delete all PreAlert records by PreAlertMaster ID
     *
     * @param preAlertMasterRefId PreAlertMaster reference ID
     * @return Success/Failure message
     */
    @DeleteMapping("/master/{preAlertMasterRefId}/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> deleteByPreAlertMasterId(@PathVariable Integer preAlertMasterRefId) {
        logger.info("Deleting all PreAlert records for master: {}", preAlertMasterRefId);

        try {
            preAlertService.deleteByPreAlertMasterId(preAlertMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting PreAlert records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting PreAlert records: " + e.getMessage());
        }
    }

    /**
     * Get count of PreAlert records by PreAlertMaster ID
     *
     * @param preAlertMasterRefId PreAlertMaster reference ID
     * @return Count of records
     */
    @GetMapping("/master/{preAlertMasterRefId}/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<Long> countByPreAlertMasterId(
            @PathVariable Integer preAlertMasterRefId) {
        logger.info("Counting PreAlert records for master: {}", preAlertMasterRefId);
        Long count = preAlertService.countByPreAlertMasterId(preAlertMasterRefId);
        return ResponseEntity.ok(count);
    }

    /**
     * Activate PreAlert record
     *
     * @param id PreAlert ID
     * @return Activated PreAlert record
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating PreAlert with ID: {}", id);

        try {
            PreAlertDto activated = preAlertService.activate(id);
            return ResponseEntity.ok(activated);
        } catch (Exception e) {
            logger.error("Error activating PreAlert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating PreAlert: " + e.getMessage());
        }
    }

    /**
     * Deactivate PreAlert record
     *
     * @param id PreAlert ID
     * @return Deactivated PreAlert record
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating PreAlert with ID: {}", id);

        try {
            PreAlertDto deactivated = preAlertService.deactivate(id);
            return ResponseEntity.ok(deactivated);
        } catch (Exception e) {
            logger.error("Error deactivating PreAlert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deactivating PreAlert: " + e.getMessage());
        }
    }
}

