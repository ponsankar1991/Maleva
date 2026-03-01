package my.maleva.api.controller;

import my.maleva.api.dto.VesselPlanningMasterDto;
import my.maleva.api.dto.VesselPlanningDetailsDto;
import my.maleva.api.service.VesselPlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * VesselPlanningController - REST Controller for VesselPlanning API
 */
@RestController
@RequestMapping("/api/vessel-plannings")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
public class VesselPlanningController {

    private static final Logger logger = LoggerFactory.getLogger(VesselPlanningController.class);

    @Autowired
    private VesselPlanningService service;

    /**
     * Get all VesselPlanning records by company ID
     * GET /api/vessel-plannings/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<VesselPlanningMasterDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching VesselPlanning for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get active VesselPlanning records by company
     * GET /api/vessel-plannings/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<List<VesselPlanningMasterDto>> getActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active VesselPlanning for company: {}", companyRefId);
        return ResponseEntity.ok(service.getActiveByCompanyRefId(companyRefId));
    }

    /**
     * Get VesselPlanning by C Number
     * GET /api/vessel-plannings/c-number/{cNumber}/company/{companyRefId}
     */
    @GetMapping("/c-number/{cNumber}/company/{companyRefId}")
    public ResponseEntity<?> getByCNumber(@PathVariable Integer cNumber, @PathVariable Integer companyRefId) {
        logger.info("Fetching VesselPlanning by C Number: {} for company: {}", cNumber, companyRefId);
        Optional<VesselPlanningMasterDto> record = service.getByCNumber(cNumber, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get VesselPlanning records by user ID
     * GET /api/vessel-plannings/user/{userRefId}
     */
    @GetMapping("/user/{userRefId}")
    public ResponseEntity<List<VesselPlanningMasterDto>> getByUserRefId(@PathVariable Integer userRefId) {
        logger.info("Fetching VesselPlanning for user: {}", userRefId);
        return ResponseEntity.ok(service.getByUserRefId(userRefId));
    }

    /**
     * Get VesselPlanning records by employee ID
     * GET /api/vessel-plannings/employee/{employeeRefId}
     */
    @GetMapping("/employee/{employeeRefId}")
    public ResponseEntity<List<VesselPlanningMasterDto>> getByEmployeeRefId(@PathVariable Integer employeeRefId) {
        logger.info("Fetching VesselPlanning for employee: {}", employeeRefId);
        return ResponseEntity.ok(service.getByEmployeeRefId(employeeRefId));
    }

    /**
     * Get VesselPlanning by ID
     * GET /api/vessel-plannings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching VesselPlanning by ID: {}", id);
        Optional<VesselPlanningMasterDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new VesselPlanning
     * POST /api/vessel-plannings
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody VesselPlanningMasterDto dto) {
        logger.info("Creating new VesselPlanning");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Process VesselPlanning (SP_VESSELPLANINGMaster logic - INSERT or UPDATE with details)
     * POST /api/vessel-plannings/process?companyId=1
     */
    @PostMapping("/process")
    public ResponseEntity<?> processVesselPlanning(
            @Valid @RequestBody VesselPlanningRequest request,
            @RequestParam Integer companyId) {
        logger.info("Processing VesselPlanning with SP_VESSELPLANINGMaster logic for company: {}", companyId);
        try {
            VesselPlanningMasterDto result = service.processVesselPlanning(
                    request.getVesselPlanning(),
                    request.getDetails(),
                    companyId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update VesselPlanning
     * PUT /api/vessel-plannings/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody VesselPlanningMasterDto dto) {
        logger.info("Updating VesselPlanning with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete VesselPlanning
     * DELETE /api/vessel-plannings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting VesselPlanning with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Activate VesselPlanning
     * PUT /api/vessel-plannings/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateVesselPlanning(@PathVariable Integer id) {
        logger.info("Activating VesselPlanning with ID: {}", id);
        try {
            return ResponseEntity.ok(service.activateVesselPlanning(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Deactivate VesselPlanning
     * PUT /api/vessel-plannings/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateVesselPlanning(@PathVariable Integer id) {
        logger.info("Deactivating VesselPlanning with ID: {}", id);
        try {
            return ResponseEntity.ok(service.deactivateVesselPlanning(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Count VesselPlanning records by company ID
     * GET /api/vessel-plannings/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting VesselPlanning for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active VesselPlanning records by company
     * GET /api/vessel-plannings/company/{companyRefId}/active/count
     */
    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<?> countActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting active VesselPlanning for company: {}", companyRefId);
        long count = service.countActiveByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Inner class for VesselPlanning request with details
     */
    public static class VesselPlanningRequest {
        private VesselPlanningMasterDto vesselPlanning;
        private List<VesselPlanningDetailsDto> details;

        public VesselPlanningMasterDto getVesselPlanning() {
            return vesselPlanning;
        }

        public void setVesselPlanning(VesselPlanningMasterDto vesselPlanning) {
            this.vesselPlanning = vesselPlanning;
        }

        public List<VesselPlanningDetailsDto> getDetails() {
            return details;
        }

        public void setDetails(List<VesselPlanningDetailsDto> details) {
            this.details = details;
        }
    }
}

