package my.maleva.api.module.saleorder.controller;

import my.maleva.api.module.saleorder.dto.SaleOrderPickupDto;
import my.maleva.api.module.saleorder.service.SaleOrderPickupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * SaleOrderPickupController - REST Controller for SaleOrderPickup API
 */
@RestController
@RequestMapping("/api/sale-order-pickups")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SaleOrderPickupController {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderPickupController.class);

    @Autowired
    private SaleOrderPickupService service;

    /**
     * Get all SaleOrderPickup records by SaleOrderMasterRefId
     * GET /api/sale-order-pickups/sale-order/{saleOrderMasterRefId}
     */
    @GetMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<List<SaleOrderPickupDto>> getBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderPickup for master: {}", saleOrderMasterRefId);
        return ResponseEntity.ok(service.getBySaleOrderMasterRefId(saleOrderMasterRefId));
    }

    /**
     * Get SaleOrderPickup by ID
     * GET /api/sale-order-pickups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleOrderPickup by ID: {}", id);
        Optional<SaleOrderPickupDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SaleOrderPickup
     * POST /api/sale-order-pickups
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleOrderPickupDto dto) {
        logger.info("Creating new SaleOrderPickup");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SaleOrderPickup
     * PUT /api/sale-order-pickups/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleOrderPickupDto dto) {
        logger.info("Updating SaleOrderPickup with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SaleOrderPickup
     * DELETE /api/sale-order-pickups/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleOrderPickup with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count SaleOrderPickup records by SaleOrderMasterRefId
     * GET /api/sale-order-pickups/count/{saleOrderMasterRefId}
     */
    @GetMapping("/count/{saleOrderMasterRefId}")
    public ResponseEntity<?> countBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderPickup for master: {}", saleOrderMasterRefId);
        long count = service.countBySaleOrderMasterRefId(saleOrderMasterRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Delete all SaleOrderPickup records by SaleOrderMasterRefId
     * DELETE /api/sale-order-pickups/sale-order/{saleOrderMasterRefId}
     */
    @DeleteMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<?> deleteBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderPickup for master: {}", saleOrderMasterRefId);
        try {
            service.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}

