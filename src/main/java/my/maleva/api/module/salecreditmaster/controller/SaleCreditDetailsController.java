package my.maleva.api.module.salecreditmaster.controller;

import my.maleva.api.module.salecreditmaster.dto.SaleCreditDetailsDto;
import my.maleva.api.module.salecreditmaster.service.SaleCreditDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * SaleCreditDetailsController
 * REST Controller for SaleCreditDetails API
 */
@RestController
@RequestMapping("/api/sale-credit-details")
@PermitAll
public class SaleCreditDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(SaleCreditDetailsController.class);

    @Autowired
    private SaleCreditDetailsService saleCreditDetailsService;

    /**
     * Get all SaleCreditDetails by Sale Credit Master Reference ID
     * GET /api/sale-credit-details/sale-credit-master/{saleCreditMasterRefId}
     */
    @GetMapping("/sale-credit-master/{saleCreditMasterRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditDetailsDto>> getBySaleCreditMasterRefId(@PathVariable Integer saleCreditMasterRefId) {
        logger.info("Fetching SaleCreditDetails by Sale Credit Master Reference ID: {}", saleCreditMasterRefId);
        List<SaleCreditDetailsDto> records = saleCreditDetailsService.getBySaleCreditMasterRefId(saleCreditMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditDetails by ID
     * GET /api/sale-credit-details/{id}
     */
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleCreditDetails by ID: {}", id);
        Optional<SaleCreditDetailsDto> record = saleCreditDetailsService.getById(id);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditDetails not found with ID: " + id);
    }

    /**
     * Create SaleCreditDetails record
     * POST /api/sale-credit-details
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<?> create(@Valid @RequestBody SaleCreditDetailsDto dto) {
        logger.info("Creating new SaleCreditDetails for Sale Credit Master: {}", dto.getSaleCreditMasterRefId());
        try {
            SaleCreditDetailsDto created = saleCreditDetailsService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating SaleCreditDetails: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating SaleCreditDetails: " + e.getMessage());
        }
    }

    /**
     * Update SaleCreditDetails record
     * PUT /api/sale-credit-details/{id}
     */
    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleCreditDetailsDto dto) {
        logger.info("Updating SaleCreditDetails with ID: {}", id);
        try {
            SaleCreditDetailsDto updated = saleCreditDetailsService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("SaleCreditDetails not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditDetails not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating SaleCreditDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating SaleCreditDetails: " + e.getMessage());
        }
    }

    /**
     * Delete SaleCreditDetails record
     * DELETE /api/sale-credit-details/{id}
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleCreditDetails with ID: {}", id);
        boolean deleted = saleCreditDetailsService.delete(id);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditDetails not found with ID: " + id);
    }

    /**
     * Get SaleCreditDetails by item ID
     * GET /api/sale-credit-details/item/{itemMasterRefId}
     */
    @GetMapping("/item/{itemMasterRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditDetailsDto>> getByItemMasterRefId(@PathVariable Integer itemMasterRefId) {
        logger.info("Fetching SaleCreditDetails by Item Master Reference ID: {}", itemMasterRefId);
        List<SaleCreditDetailsDto> records = saleCreditDetailsService.getByItemMasterRefId(itemMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Count SaleCreditDetails by Sale Credit Master Reference ID
     * GET /api/sale-credit-details/count/sale-credit-master/{saleCreditMasterRefId}
     */
    @GetMapping("/count/sale-credit-master/{saleCreditMasterRefId}")
    @PermitAll
    public ResponseEntity<Long> countBySaleCreditMasterRefId(@PathVariable Integer saleCreditMasterRefId) {
        logger.info("Counting SaleCreditDetails for Sale Credit Master: {}", saleCreditMasterRefId);
        long count = saleCreditDetailsService.countBySaleCreditMasterRefId(saleCreditMasterRefId);
        return ResponseEntity.ok(count);
    }

    /**
     * Delete all SaleCreditDetails for a Sale Credit Master
     * DELETE /api/sale-credit-details/sale-credit-master/{saleCreditMasterRefId}
     */
    @DeleteMapping("/sale-credit-master/{saleCreditMasterRefId}")
    @PermitAll
    public ResponseEntity<?> deleteAllBySaleCreditMasterRefId(@PathVariable Integer saleCreditMasterRefId) {
        logger.info("Deleting all SaleCreditDetails for Sale Credit Master: {}", saleCreditMasterRefId);
        try {
            saleCreditDetailsService.deleteAllBySaleCreditMasterRefId(saleCreditMasterRefId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            logger.error("Error deleting SaleCreditDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting SaleCreditDetails: " + e.getMessage());
        }
    }
}


