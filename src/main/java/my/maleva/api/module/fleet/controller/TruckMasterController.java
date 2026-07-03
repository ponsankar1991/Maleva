package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.fleet.dto.TruckMasterDto;
import my.maleva.api.module.fleet.service.TruckMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.fleet.dto.SearchResultDto;

import java.util.List;
import java.util.Optional;

/**
 * TruckMasterController - REST Controller for TruckMaster API
 */
@RestController
@RequestMapping("/api/truck-masters")
@PermitAll
public class TruckMasterController {

    private static final Logger logger = LoggerFactory.getLogger(TruckMasterController.class);

    @Autowired
    private TruckMasterService service;

    /**
     * Get all TruckMaster records by company ID
     * GET /api/truck-masters/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<TruckMasterDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching TruckMaster for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get active TruckMaster records by company
     * GET /api/truck-masters/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<List<TruckMasterDto>> getActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active TruckMaster for company: {}", companyRefId);
        return ResponseEntity.ok(service.getActiveByCompanyRefId(companyRefId));
    }

    /**
     * Get TruckMaster by truck name
     * GET /api/truck-masters/name/{truckName}/company/{companyRefId}
     */
    @GetMapping("/name/{truckName}/company/{companyRefId}")
    public ResponseEntity<?> getByTruckName(@PathVariable String truckName, @PathVariable Integer companyRefId) {
        logger.info("Fetching TruckMaster by name: {} for company: {}", truckName, companyRefId);
        Optional<TruckMasterDto> record = service.getByTruckName(truckName, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get TruckMaster by truck number
     * GET /api/truck-masters/number/{truckNumber}/company/{companyRefId}
     */
    @GetMapping("/number/{truckNumber}/company/{companyRefId}")
    public ResponseEntity<?> getByTruckNumber(@PathVariable String truckNumber, @PathVariable Integer companyRefId) {
        logger.info("Fetching TruckMaster by number: {} for company: {}", truckNumber, companyRefId);
        Optional<TruckMasterDto> record = service.getByTruckNumber(truckNumber, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get TruckMaster by C Number
     * GET /api/truck-masters/c-number/{cNumber}/company/{companyRefId}
     */
    @GetMapping("/c-number/{cNumber}/company/{companyRefId}")
    public ResponseEntity<?> getByCNumber(@PathVariable Integer cNumber, @PathVariable Integer companyRefId) {
        logger.info("Fetching TruckMaster by C Number: {} for company: {}", cNumber, companyRefId);
        Optional<TruckMasterDto> record = service.getByCNumber(cNumber, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get TruckMaster records by truck type
     * GET /api/truck-masters/type/{truckType}
     */
    @GetMapping("/type/{truckType}")
    public ResponseEntity<List<TruckMasterDto>> getByTruckType(@PathVariable String truckType) {
        logger.info("Fetching TruckMaster for type: {}", truckType);
        return ResponseEntity.ok(service.getByTruckType(truckType));
    }

    /**
     * Get TruckMaster records by company and truck type
     * GET /api/truck-masters/company/{companyRefId}/type/{truckType}
     */
    @GetMapping("/company/{companyRefId}/type/{truckType}")
    public ResponseEntity<List<TruckMasterDto>> getByCompanyAndTruckType(
            @PathVariable Integer companyRefId,
            @PathVariable String truckType) {
        logger.info("Fetching TruckMaster for company: {} and type: {}", companyRefId, truckType);
        return ResponseEntity.ok(service.getByCompanyAndTruckType(companyRefId, truckType));
    }

    /**
     * Get TruckMaster records by vehicle type
     * GET /api/truck-masters/vehicle-type/{vehicleType}
     */
    @GetMapping("/vehicle-type/{vehicleType}")
    public ResponseEntity<List<TruckMasterDto>> getByVehicleType(@PathVariable String vehicleType) {
        logger.info("Fetching TruckMaster for vehicle type: {}", vehicleType);
        return ResponseEntity.ok(service.getByVehicleType(vehicleType));
    }

    /**
     * Get TruckMaster by ID
     * GET /api/truck-masters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching TruckMaster by ID: {}", id);
        Optional<TruckMasterDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new TruckMaster
     * POST /api/truck-masters
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TruckMasterDto dto) {
        logger.info("Creating new TruckMaster");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Process TruckMaster (SP_Truck logic - INSERT or UPDATE)
     * POST /api/truck-masters/process?companyId=1
     */
    @PostMapping("/process")
    public ResponseEntity<?> processTruck(
            @Valid @RequestBody TruckMasterDto dto,
            @RequestParam Integer companyId) {
        logger.info("Processing TruckMaster with SP_Truck logic for company: {}", companyId);
        try {
            TruckMasterDto result = service.processTruck(dto, companyId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update TruckMaster
     * PUT /api/truck-masters/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody TruckMasterDto dto) {
        logger.info("Updating TruckMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete TruckMaster
     * DELETE /api/truck-masters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting TruckMaster with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Search trucks with pagination and filters
     * GET /api/truck-masters/search?companyId=&startIndex=&pageCount=&keyword=&column=&type=
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResultDto>> searchTrucks(
            @RequestParam Integer companyId,
            @RequestParam(required = false, defaultValue = "0") Integer startIndex,
            @RequestParam(required = false, defaultValue = "0") Integer pageCount,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "All") String column,
            @RequestParam(required = false) String type) {

        SearchResultDto result = service.searchTrucks(companyId, startIndex, pageCount, keyword, column, type);
        return ResponseEntity.ok(ApiResponse.success(result, "Success"));
    }

    /**
     * Activate TruckMaster
     * PUT /api/truck-masters/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateTruck(@PathVariable Integer id) {
        logger.info("Activating TruckMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.activateTruck(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Deactivate TruckMaster
     * PUT /api/truck-masters/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateTruck(@PathVariable Integer id) {
        logger.info("Deactivating TruckMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.deactivateTruck(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Count TruckMaster records by company ID
     * GET /api/truck-masters/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting TruckMaster for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active TruckMaster records by company
     * GET /api/truck-masters/company/{companyRefId}/active/count
     */
    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<?> countActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting active TruckMaster for company: {}", companyRefId);
        long count = service.countActiveByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Check if TruckMaster exists by truck number
     * GET /api/truck-masters/number/{truckNumber}/company/{companyRefId}/exists
     */
    @GetMapping("/number/{truckNumber}/company/{companyRefId}/exists")
    public ResponseEntity<?> existsByTruckNumber(@PathVariable String truckNumber, @PathVariable Integer companyRefId) {
        logger.info("Checking if TruckMaster exists with number: {} for company: {}", truckNumber, companyRefId);
        boolean exists = service.existsByTruckNumber(truckNumber, companyRefId);
        return ResponseEntity.ok("Exists: " + exists);
    }

    @GetMapping("/alltruckdetatilcombo")
    public ResponseEntity<ApiResponse<java.util.List<my.maleva.api.module.fleet.dto.TruckMasterDto>>> getAllTruckDetailCombo(
            @RequestParam Integer companyId,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "All") String column,
            @RequestParam(required = false) String type) {

        java.util.List<my.maleva.api.module.fleet.dto.TruckMasterDto> result = service.getAllTruckDetailCombo(companyId, keyword, column, type);
        return ResponseEntity.ok(ApiResponse.success(result, "Success"));
    }
}

