package my.maleva.api.controller;

import my.maleva.api.dto.SubcdiyEntryDto;
import my.maleva.api.service.SubcdiyEntryService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SubcdiyEntryController - REST Controller for SubcdiyEntry API
 */
@RestController
@RequestMapping("/api/subcdiy-entries")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
public class SubcdiyEntryController {

    private static final Logger logger = LoggerFactory.getLogger(SubcdiyEntryController.class);

    @Autowired
    private SubcdiyEntryService service;

    /**
     * Get all SubcdiyEntry records by active status
     * GET /api/subcdiy-entries/active/{active}
     */
    @GetMapping("/active/{active}")
    public ResponseEntity<List<SubcdiyEntryDto>> getByActive(@PathVariable Integer active) {
        logger.info("Fetching SubcdiyEntry for active status: {}", active);
        return ResponseEntity.ok(service.getByActive(active));
    }

    /**
     * Get all active SubcdiyEntry records
     * GET /api/subcdiy-entries/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<SubcdiyEntryDto>> getAllActive() {
        logger.info("Fetching all active SubcdiyEntry records");
        return ResponseEntity.ok(service.getAllActive());
    }

    /**
     * Get SubcdiyEntry records by entry date
     * GET /api/subcdiy-entries/date/{entryDate}
     */
    @GetMapping("/date/{entryDate}")
    public ResponseEntity<List<SubcdiyEntryDto>> getByEntryDate(@PathVariable LocalDate entryDate) {
        logger.info("Fetching SubcdiyEntry for entry date: {}", entryDate);
        return ResponseEntity.ok(service.getByEntryDate(entryDate));
    }

    /**
     * Get SubcdiyEntry records by date range
     * GET /api/subcdiy-entries/date-range?startDate=2026-01-01&endDate=2026-12-31
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<SubcdiyEntryDto>> getByEntryDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        logger.info("Fetching SubcdiyEntry for date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(service.getByEntryDateRange(startDate, endDate));
    }

    /**
     * Get SubcdiyEntry records by amount range
     * GET /api/subcdiy-entries/amount-range?minAmount=100&maxAmount=1000
     */
    @GetMapping("/amount-range")
    public ResponseEntity<List<SubcdiyEntryDto>> getByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        logger.info("Fetching SubcdiyEntry for amount range: {} to {}", minAmount, maxAmount);
        return ResponseEntity.ok(service.getByAmountRange(minAmount, maxAmount));
    }

    /**
     * Get SubcdiyEntry by ID
     * GET /api/subcdiy-entries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SubcdiyEntry by ID: {}", id);
        Optional<SubcdiyEntryDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SubcdiyEntry
     * POST /api/subcdiy-entries
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SubcdiyEntryDto dto) {
        logger.info("Creating new SubcdiyEntry");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SubcdiyEntry
     * PUT /api/subcdiy-entries/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SubcdiyEntryDto dto) {
        logger.info("Updating SubcdiyEntry with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SubcdiyEntry
     * DELETE /api/subcdiy-entries/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SubcdiyEntry with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Activate SubcdiyEntry
     * PUT /api/subcdiy-entries/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateEntry(@PathVariable Integer id) {
        logger.info("Activating SubcdiyEntry with ID: {}", id);
        try {
            return ResponseEntity.ok(service.activateEntry(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Deactivate SubcdiyEntry
     * PUT /api/subcdiy-entries/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateEntry(@PathVariable Integer id) {
        logger.info("Deactivating SubcdiyEntry with ID: {}", id);
        try {
            return ResponseEntity.ok(service.deactivateEntry(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Count SubcdiyEntry records by active status
     * GET /api/subcdiy-entries/active/{active}/count
     */
    @GetMapping("/active/{active}/count")
    public ResponseEntity<?> countByActive(@PathVariable Integer active) {
        logger.info("Counting SubcdiyEntry for active status: {}", active);
        long count = service.countByActive(active);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Check if SubcdiyEntry exists by entry date
     * GET /api/subcdiy-entries/date/{entryDate}/exists
     */
    @GetMapping("/date/{entryDate}/exists")
    public ResponseEntity<?> existsByEntryDate(@PathVariable LocalDate entryDate) {
        logger.info("Checking if SubcdiyEntry exists for date: {}", entryDate);
        boolean exists = service.existsByEntryDate(entryDate);
        return ResponseEntity.ok("Exists: " + exists);
    }
}

