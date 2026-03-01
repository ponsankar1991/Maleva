package my.maleva.api.controller;

import my.maleva.api.dto.SummonDto;
import my.maleva.api.service.SummonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * SummonController - REST Controller for Summon API
 */
@RestController
@RequestMapping("/api/summons")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
public class SummonController {

    private static final Logger logger = LoggerFactory.getLogger(SummonController.class);

    @Autowired
    private SummonService service;

    /**
     * Get all Summon records by truck name
     * GET /api/summons/truck/{truckName}
     */
    @GetMapping("/truck/{truckName}")
    public ResponseEntity<List<SummonDto>> getByTruckName(@PathVariable String truckName) {
        logger.info("Fetching Summon for truck: {}", truckName);
        return ResponseEntity.ok(service.getByTruckName(truckName));
    }

    /**
     * Get all Summon records by driver name
     * GET /api/summons/driver/{driverName}
     */
    @GetMapping("/driver/{driverName}")
    public ResponseEntity<List<SummonDto>> getByDriverName(@PathVariable String driverName) {
        logger.info("Fetching Summon for driver: {}", driverName);
        return ResponseEntity.ok(service.getByDriverName(driverName));
    }

    /**
     * Get all Summon records by company ID
     * GET /api/summons/company/{comid}
     */
    @GetMapping("/company/{comid}")
    public ResponseEntity<List<SummonDto>> getByComid(@PathVariable Integer comid) {
        logger.info("Fetching Summon for company: {}", comid);
        return ResponseEntity.ok(service.getByComid(comid));
    }

    /**
     * Get Summon records by entry date
     * GET /api/summons/date/{entryDate}
     */
    @GetMapping("/date/{entryDate}")
    public ResponseEntity<List<SummonDto>> getByEntryDate(@PathVariable LocalDate entryDate) {
        logger.info("Fetching Summon for entry date: {}", entryDate);
        return ResponseEntity.ok(service.getByEntryDate(entryDate));
    }

    /**
     * Get Summon records by date range
     * GET /api/summons/date-range?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<SummonDto>> getByEntryDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        logger.info("Fetching Summon for date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(service.getByEntryDateRange(startDate, endDate));
    }

    /**
     * Get Summon records by amount range
     * GET /api/summons/amount-range?minAmount=100&maxAmount=1000
     */
    @GetMapping("/amount-range")
    public ResponseEntity<List<SummonDto>> getByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        logger.info("Fetching Summon for amount range: {} to {}", minAmount, maxAmount);
        return ResponseEntity.ok(service.getByAmountRange(minAmount, maxAmount));
    }

    /**
     * Get Summon records by country
     * GET /api/summons/country/{country}
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<List<SummonDto>> getByCountry(@PathVariable String country) {
        logger.info("Fetching Summon for country: {}", country);
        return ResponseEntity.ok(service.getByCountry(country));
    }

    /**
     * Get Summon by ID
     * GET /api/summons/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching Summon by ID: {}", id);
        Optional<SummonDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get Summon by truck and driver name
     * GET /api/summons/search?truckName=Truck1&driverName=Driver1
     */
    @GetMapping("/search")
    public ResponseEntity<?> getByTruckAndDriver(
            @RequestParam String truckName,
            @RequestParam String driverName) {
        logger.info("Fetching Summon by truck: {} and driver: {}", truckName, driverName);
        Optional<SummonDto> record = service.getByTruckAndDriver(truckName, driverName);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new Summon
     * POST /api/summons
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SummonDto dto) {
        logger.info("Creating new Summon");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Process Summon (SP_Summon logic - INSERT or UPDATE)
     * POST /api/summons/process?comid=1
     */
    @PostMapping("/process")
    public ResponseEntity<?> processSummon(
            @Valid @RequestBody SummonDto dto,
            @RequestParam Integer comid) {
        logger.info("Processing Summon with SP_Summon logic for company: {}", comid);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.processSummon(dto, comid));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update Summon
     * PUT /api/summons/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SummonDto dto) {
        logger.info("Updating Summon with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete Summon
     * DELETE /api/summons/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting Summon with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count Summon records by company ID
     * GET /api/summons/company/{comid}/count
     */
    @GetMapping("/company/{comid}/count")
    public ResponseEntity<?> countByComid(@PathVariable Integer comid) {
        logger.info("Counting Summon for company: {}", comid);
        long count = service.countByComid(comid);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count Summon records by country
     * GET /api/summons/country/{country}/count
     */
    @GetMapping("/country/{country}/count")
    public ResponseEntity<?> countByCountry(@PathVariable String country) {
        logger.info("Counting Summon for country: {}", country);
        long count = service.countByCountry(country);
        return ResponseEntity.ok("Total: " + count);
    }
}

