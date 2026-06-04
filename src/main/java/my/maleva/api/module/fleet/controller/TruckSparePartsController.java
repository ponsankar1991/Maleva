package my.maleva.api.module.fleet.controller;

import my.maleva.api.module.fleet.dto.TruckSparePartsDto;
import my.maleva.api.module.fleet.service.TruckSparePartsService;
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
 * TruckSparePartsController - REST Controller for TruckSpareParts API
 */
@RestController
@RequestMapping("/api/truck-spare-parts")
@PermitAll
public class TruckSparePartsController {

    private static final Logger logger = LoggerFactory.getLogger(TruckSparePartsController.class);

    @Autowired
    private TruckSparePartsService service;

    /**
     * Get all TruckSpareParts records by company ID
     * GET /api/truck-spare-parts/company/{comid}
     */
    @GetMapping("/company/{comid}")
    public ResponseEntity<List<TruckSparePartsDto>> getByComid(@PathVariable Integer comid) {
        logger.info("Fetching TruckSpareParts for company: {}", comid);
        return ResponseEntity.ok(service.getByComid(comid));
    }

    /**
     * Get TruckSpareParts records by truck name
     * GET /api/truck-spare-parts/truck/{truckName}
     */
    @GetMapping("/truck/{truckName}")
    public ResponseEntity<List<TruckSparePartsDto>> getByTruckName(@PathVariable String truckName) {
        logger.info("Fetching TruckSpareParts for truck: {}", truckName);
        return ResponseEntity.ok(service.getByTruckName(truckName));
    }

    /**
     * Get TruckSpareParts records by truck name and company
     * GET /api/truck-spare-parts/truck/{truckName}/company/{comid}
     */
    @GetMapping("/truck/{truckName}/company/{comid}")
    public ResponseEntity<List<TruckSparePartsDto>> getByTruckNameAndComid(
            @PathVariable String truckName,
            @PathVariable Integer comid) {
        logger.info("Fetching TruckSpareParts for truck: {} and company: {}", truckName, comid);
        return ResponseEntity.ok(service.getByTruckNameAndComid(truckName, comid));
    }

    /**
     * Get TruckSpareParts records by driver name
     * GET /api/truck-spare-parts/driver/{driverName}
     */
    @GetMapping("/driver/{driverName}")
    public ResponseEntity<List<TruckSparePartsDto>> getByDriverName(@PathVariable String driverName) {
        logger.info("Fetching TruckSpareParts for driver: {}", driverName);
        return ResponseEntity.ok(service.getByDriverName(driverName));
    }

    /**
     * Get TruckSpareParts records by driver name and company
     * GET /api/truck-spare-parts/driver/{driverName}/company/{comid}
     */
    @GetMapping("/driver/{driverName}/company/{comid}")
    public ResponseEntity<List<TruckSparePartsDto>> getByDriverNameAndComid(
            @PathVariable String driverName,
            @PathVariable Integer comid) {
        logger.info("Fetching TruckSpareParts for driver: {} and company: {}", driverName, comid);
        return ResponseEntity.ok(service.getByDriverNameAndComid(driverName, comid));
    }

    /**
     * Get TruckSpareParts records by spare parts type
     * GET /api/truck-spare-parts/parts/{spareParts}
     */
    @GetMapping("/parts/{spareParts}")
    public ResponseEntity<List<TruckSparePartsDto>> getBySpareParts(@PathVariable String spareParts) {
        logger.info("Fetching TruckSpareParts for spare parts: {}", spareParts);
        return ResponseEntity.ok(service.getBySpareParts(spareParts));
    }

    /**
     * Get TruckSpareParts by ID
     * GET /api/truck-spare-parts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching TruckSpareParts by ID: {}", id);
        Optional<TruckSparePartsDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new TruckSpareParts
     * POST /api/truck-spare-parts
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TruckSparePartsDto dto) {
        logger.info("Creating new TruckSpareParts");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Process TruckSpareParts (SP_TruckSpareParts logic - INSERT or UPDATE)
     * POST /api/truck-spare-parts/process?comid=1
     */
    @PostMapping("/process")
    public ResponseEntity<?> processTruckSpareParts(
            @Valid @RequestBody TruckSparePartsDto dto,
            @RequestParam Integer comid) {
        logger.info("Processing TruckSpareParts with SP_TruckSpareParts logic for company: {}", comid);
        try {
            TruckSparePartsDto result = service.processTruckSpareParts(dto, comid);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update TruckSpareParts
     * PUT /api/truck-spare-parts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody TruckSparePartsDto dto) {
        logger.info("Updating TruckSpareParts with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete TruckSpareParts
     * DELETE /api/truck-spare-parts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting TruckSpareParts with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count TruckSpareParts records by company ID
     * GET /api/truck-spare-parts/company/{comid}/count
     */
    @GetMapping("/company/{comid}/count")
    public ResponseEntity<?> countByComid(@PathVariable Integer comid) {
        logger.info("Counting TruckSpareParts for company: {}", comid);
        long count = service.countByComid(comid);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count TruckSpareParts records by truck name and company
     * GET /api/truck-spare-parts/truck/{truckName}/company/{comid}/count
     */
    @GetMapping("/truck/{truckName}/company/{comid}/count")
    public ResponseEntity<?> countByTruckNameAndComid(
            @PathVariable String truckName,
            @PathVariable Integer comid) {
        logger.info("Counting TruckSpareParts for truck: {} and company: {}", truckName, comid);
        long count = service.countByTruckNameAndComid(truckName, comid);
        return ResponseEntity.ok("Total: " + count);
    }
}


