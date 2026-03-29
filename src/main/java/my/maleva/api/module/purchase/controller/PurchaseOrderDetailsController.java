package my.maleva.api.module.purchase.controller;

import my.maleva.api.module.purchase.dto.PurchaseOrderDetailsDto;
import my.maleva.api.module.purchase.service.PurchaseOrderDetailsService;
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
 * PurchaseOrderDetails REST Controller
 * Handles all RESTful API endpoints for PurchaseOrderDetails operations
 * Base URL: /api/purchase-order-details
 */
@RestController
@RequestMapping("/api/purchase-order-details")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PurchaseOrderDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderDetailsController.class);

    @Autowired
    private PurchaseOrderDetailsService purchaseOrderDetailsService;

    /**
     * Get all PurchaseOrderDetails by PurchaseOrderMaster ID
     * GET /api/purchase-order-details/purchase-order/{purchaseOrderMasterRefId}
     */
    @GetMapping("/purchase-order/{purchaseOrderMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseOrderDetailsDto>> getByPurchaseOrderMasterId(
            @PathVariable Integer purchaseOrderMasterRefId) {
        logger.info("Fetching PurchaseOrderDetails for PurchaseOrderMaster: {}", purchaseOrderMasterRefId);
        List<PurchaseOrderDetailsDto> records = purchaseOrderDetailsService.getByPurchaseOrderMasterId(purchaseOrderMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseOrderDetails by ID
     * GET /api/purchase-order-details/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching PurchaseOrderDetails by ID: {}", id);
        Optional<PurchaseOrderDetailsDto> record = purchaseOrderDetailsService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseOrderDetails not found with ID: " + id);
        }
    }

    /**
     * Create new PurchaseOrderDetails record
     * POST /api/purchase-order-details
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody PurchaseOrderDetailsDto dto) {
        logger.info("Creating new PurchaseOrderDetails for PurchaseOrderMaster: {}", dto.getPurchaseOrderMasterRefId());

        try {
            PurchaseOrderDetailsDto created = purchaseOrderDetailsService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating PurchaseOrderDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating PurchaseOrderDetails: " + e.getMessage());
        }
    }

    /**
     * Update PurchaseOrderDetails record
     * PUT /api/purchase-order-details/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody PurchaseOrderDetailsDto dto) {
        logger.info("Updating PurchaseOrderDetails with ID: {}", id);

        try {
            PurchaseOrderDetailsDto updated = purchaseOrderDetailsService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("PurchaseOrderDetails not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseOrderDetails not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating PurchaseOrderDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating PurchaseOrderDetails: " + e.getMessage());
        }
    }

    /**
     * Delete PurchaseOrderDetails record
     * DELETE /api/purchase-order-details/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting PurchaseOrderDetails with ID: {}", id);

        try {
            boolean deleted = purchaseOrderDetailsService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("PurchaseOrderDetails not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting PurchaseOrderDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting PurchaseOrderDetails: " + e.getMessage());
        }
    }

    /**
     * Get PurchaseOrderDetails by product
     * GET /api/purchase-order-details/product/{productMasterRefId}
     */
    @GetMapping("/product/{productMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseOrderDetailsDto>> getByProductMasterId(
            @PathVariable Integer productMasterRefId) {
        logger.info("Fetching PurchaseOrderDetails by product: {}", productMasterRefId);
        List<PurchaseOrderDetailsDto> records = purchaseOrderDetailsService.getByProductMasterId(productMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Count PurchaseOrderDetails for a PurchaseOrderMaster
     * GET /api/purchase-order-details/purchase-order/{purchaseOrderMasterRefId}/count
     */
    @GetMapping("/purchase-order/{purchaseOrderMasterRefId}/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> countByPurchaseOrderMasterId(
            @PathVariable Integer purchaseOrderMasterRefId) {
        logger.info("Counting PurchaseOrderDetails for PurchaseOrderMaster: {}", purchaseOrderMasterRefId);
        long count = purchaseOrderDetailsService.countByPurchaseOrderMasterId(purchaseOrderMasterRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Delete all PurchaseOrderDetails for a PurchaseOrderMaster
     * DELETE /api/purchase-order-details/purchase-order/{purchaseOrderMasterRefId}
     */
    @DeleteMapping("/purchase-order/{purchaseOrderMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> deleteByPurchaseOrderMasterId(
            @PathVariable Integer purchaseOrderMasterRefId) {
        logger.info("Deleting all PurchaseOrderDetails for PurchaseOrderMaster: {}", purchaseOrderMasterRefId);

        try {
            purchaseOrderDetailsService.deleteByPurchaseOrderMasterId(purchaseOrderMasterRefId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            logger.error("Error deleting PurchaseOrderDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting PurchaseOrderDetails: " + e.getMessage());
        }
    }
}

