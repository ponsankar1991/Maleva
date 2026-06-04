package my.maleva.api.module.fleet.controller;

import my.maleva.api.module.fleet.dto.SpeedReportDto;
import my.maleva.api.module.fleet.service.SpeedReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Optional;

/**
 * SpeedReportController - REST Controller for SpeedReport API
 */
@RestController
@RequestMapping("/api/speed-reports")
@PermitAll
public class SpeedReportController {

    private static final Logger logger = LoggerFactory.getLogger(SpeedReportController.class);

    @Autowired
    private SpeedReportService service;

    /**
     * Get all SpeedReport records by company ID
     * GET /api/speed-reports/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<SpeedReportDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching SpeedReport for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get all SpeedReport records by truck ID
     * GET /api/speed-reports/truck/{truckRefId}
     */
    @GetMapping("/truck/{truckRefId}")
    public ResponseEntity<List<SpeedReportDto>> getByTruckRefId(@PathVariable Integer truckRefId) {
        logger.info("Fetching SpeedReport for truck: {}", truckRefId);
        return ResponseEntity.ok(service.getByTruckRefId(truckRefId));
    }

    /**
     * Get SpeedReport records by company and truck
     * GET /api/speed-reports/company/{companyRefId}/truck/{truckRefId}
     */
    @GetMapping("/company/{companyRefId}/truck/{truckRefId}")
    public ResponseEntity<List<SpeedReportDto>> getByCompanyAndTruck(
            @PathVariable Integer companyRefId,
            @PathVariable Integer truckRefId) {
        logger.info("Fetching SpeedReport for company: {} and truck: {}", companyRefId, truckRefId);
        return ResponseEntity.ok(service.getByCompanyAndTruck(companyRefId, truckRefId));
    }

    /**
     * Get SpeedReport by ID
     * GET /api/speed-reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SpeedReport by ID: {}", id);
        Optional<SpeedReportDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SpeedReport
     * POST /api/speed-reports
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SpeedReportDto dto) {
        logger.info("Creating new SpeedReport");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SpeedReport
     * PUT /api/speed-reports/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SpeedReportDto dto) {
        logger.info("Updating SpeedReport with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SpeedReport
     * DELETE /api/speed-reports/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SpeedReport with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count SpeedReport records by company ID
     * GET /api/speed-reports/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting SpeedReport for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count SpeedReport records by truck ID
     * GET /api/speed-reports/truck/{truckRefId}/count
     */
    @GetMapping("/truck/{truckRefId}/count")
    public ResponseEntity<?> countByTruckRefId(@PathVariable Integer truckRefId) {
        logger.info("Counting SpeedReport for truck: {}", truckRefId);
        long count = service.countByTruckRefId(truckRefId);
        return ResponseEntity.ok("Total: " + count);
    }
}


