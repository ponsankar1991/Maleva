package my.maleva.api.module.purchase.controller;

import my.maleva.api.module.purchase.dto.PurchaseDetailsDto;
import my.maleva.api.module.purchase.service.PurchaseDetailsService;
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
 * PurchaseDetails REST Controller
 * Handles all RESTful API endpoints for PurchaseDetails operations
 * Base URL: /api/purchase-details
 */
@RestController
@RequestMapping("/api/purchase-details")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PurchaseDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseDetailsController.class);

    @Autowired
    private PurchaseDetailsService purchaseDetailsService;

    /**
     * Get all PurchaseDetails by purchase master
     * GET /api/purchase-details/purchase-master/{purchaseMasterRefId}
     */
    @GetMapping("/purchase-master/{purchaseMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseDetailsDto>> getByPurchaseMaster(
            @PathVariable Integer purchaseMasterRefId) {
        logger.info("Fetching all PurchaseDetails for purchase master: {}", purchaseMasterRefId);
        List<PurchaseDetailsDto> records = purchaseDetailsService.getByPurchaseMaster(purchaseMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseDetails by ID
     * GET /api/purchase-details/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching PurchaseDetails by ID: {}", id);
        Optional<PurchaseDetailsDto> record = purchaseDetailsService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseDetails not found with ID: " + id);
        }
    }

    /**
     * Create new PurchaseDetails record
     * POST /api/purchase-details
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody PurchaseDetailsDto dto) {
        logger.info("Creating new PurchaseDetails for purchase master: {}", dto.getPurchaseMasterRefId());

        try {
            PurchaseDetailsDto created = purchaseDetailsService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating PurchaseDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating PurchaseDetails: " + e.getMessage());
        }
    }

    /**
     * Create multiple PurchaseDetails records
     * POST /api/purchase-details/batch
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> createBatch(@Valid @RequestBody List<PurchaseDetailsDto> dtos) {
        logger.info("Creating batch of {} PurchaseDetails records", dtos.size());

        try {
            List<PurchaseDetailsDto> created = purchaseDetailsService.createBatch(dtos);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating batch of PurchaseDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating batch of PurchaseDetails: " + e.getMessage());
        }
    }

    /**
     * Update PurchaseDetails record
     * PUT /api/purchase-details/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody PurchaseDetailsDto dto) {
        logger.info("Updating PurchaseDetails with ID: {}", id);

        try {
            PurchaseDetailsDto updated = purchaseDetailsService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("PurchaseDetails not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseDetails not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating PurchaseDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating PurchaseDetails: " + e.getMessage());
        }
    }

    /**
     * Delete PurchaseDetails record
     * DELETE /api/purchase-details/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting PurchaseDetails with ID: {}", id);

        try {
            boolean deleted = purchaseDetailsService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("PurchaseDetails not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting PurchaseDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting PurchaseDetails: " + e.getMessage());
        }
    }

    /**
     * Delete all PurchaseDetails by purchase master
     * DELETE /api/purchase-details/purchase-master/{purchaseMasterRefId}
     */
    @DeleteMapping("/purchase-master/{purchaseMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> deleteByPurchaseMaster(@PathVariable Integer purchaseMasterRefId) {
        logger.info("Deleting all PurchaseDetails for purchase master: {}", purchaseMasterRefId);

        try {
            boolean deleted = purchaseDetailsService.deleteByPurchaseMaster(purchaseMasterRefId);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error deleting PurchaseDetails");
            }
        } catch (Exception e) {
            logger.error("Error deleting PurchaseDetails by purchase master", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting PurchaseDetails: " + e.getMessage());
        }
    }

    /**
     * Get PurchaseDetails by product
     * GET /api/purchase-details/product/{productMasterRefId}
     */
    @GetMapping("/product/{productMasterRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseDetailsDto>> getByProduct(
            @PathVariable Integer productMasterRefId) {
        logger.info("Fetching PurchaseDetails for product: {}", productMasterRefId);
        List<PurchaseDetailsDto> records = purchaseDetailsService.getByProduct(productMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Count PurchaseDetails by purchase master
     * GET /api/purchase-details/purchase-master/{purchaseMasterRefId}/count
     */
    @GetMapping("/purchase-master/{purchaseMasterRefId}/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> countByPurchaseMaster(@PathVariable Integer purchaseMasterRefId) {
        logger.info("Counting PurchaseDetails for purchase master: {}", purchaseMasterRefId);
        long count = purchaseDetailsService.countByPurchaseMaster(purchaseMasterRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Calculate total amount for purchase details
     * GET /api/purchase-details/purchase-master/{purchaseMasterRefId}/total-amount
     */
    @GetMapping("/purchase-master/{purchaseMasterRefId}/total-amount")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> calculateTotalAmount(@PathVariable Integer purchaseMasterRefId) {
        logger.info("Calculating total amount for purchase master: {}", purchaseMasterRefId);
        Double totalAmount = purchaseDetailsService.calculateTotalAmount(purchaseMasterRefId);
        return ResponseEntity.ok("Total Amount: " + totalAmount);
    }

    /**
     * Calculate total tax for purchase details
     * GET /api/purchase-details/purchase-master/{purchaseMasterRefId}/total-tax
     */
    @GetMapping("/purchase-master/{purchaseMasterRefId}/total-tax")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> calculateTotalTax(@PathVariable Integer purchaseMasterRefId) {
        logger.info("Calculating total tax for purchase master: {}", purchaseMasterRefId);
        Double totalTax = purchaseDetailsService.calculateTotalTax(purchaseMasterRefId);
        return ResponseEntity.ok("Total Tax: " + totalTax);
    }

    /**
     * Calculate total discount for purchase details
     * GET /api/purchase-details/purchase-master/{purchaseMasterRefId}/total-discount
     */
    @GetMapping("/purchase-master/{purchaseMasterRefId}/total-discount")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> calculateTotalDiscount(@PathVariable Integer purchaseMasterRefId) {
        logger.info("Calculating total discount for purchase master: {}", purchaseMasterRefId);
        Double totalDiscount = purchaseDetailsService.calculateTotalDiscount(purchaseMasterRefId);
        return ResponseEntity.ok("Total Discount: " + totalDiscount);
    }
}

