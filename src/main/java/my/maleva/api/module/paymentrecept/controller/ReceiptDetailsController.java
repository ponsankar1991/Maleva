package my.maleva.api.module.paymentrecept.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.paymentrecept.dto.ReceiptDetailsDto;
import my.maleva.api.module.paymentrecept.service.ReceiptDetailsService;
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
 * ReceiptDetails REST Controller
 * Handles all RESTful API endpoints for ReceiptDetails operations
 * Base URL: /api/receipt-details
 */
@RestController
@RequestMapping("/api/receipt-details")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReceiptDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptDetailsController.class);

    @Autowired
    private ReceiptDetailsService receiptDetailsService;

    /**
     * Get all ReceiptDetails by Receipt ID
     * GET /api/receipt-details/receipt/{receiptRefId}
     */
    @GetMapping("/receipt/{receiptRefId}")
    @PermitAll
    public ResponseEntity<List<ReceiptDetailsDto>> getByReceiptId(
            @PathVariable Integer receiptRefId) {
        logger.info("Fetching ReceiptDetails for Receipt: {}", receiptRefId);
        List<ReceiptDetailsDto> records = receiptDetailsService.getByReceiptId(receiptRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get ReceiptDetails by ID
     * GET /api/receipt-details/{id}
     */
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching ReceiptDetails by ID: {}", id);
        Optional<ReceiptDetailsDto> record = receiptDetailsService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ReceiptDetails not found with ID: " + id);
        }
    }

    /**
     * Create new ReceiptDetails record
     * POST /api/receipt-details
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<?> create(@Valid @RequestBody ReceiptDetailsDto dto) {
        logger.info("Creating new ReceiptDetails for Receipt: {}", dto.getReceiptRefId());

        try {
            ReceiptDetailsDto created = receiptDetailsService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating ReceiptDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating ReceiptDetails: " + e.getMessage());
        }
    }

    /**
     * Update ReceiptDetails record
     * PUT /api/receipt-details/{id}
     */
    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody ReceiptDetailsDto dto) {
        logger.info("Updating ReceiptDetails with ID: {}", id);

        try {
            ReceiptDetailsDto updated = receiptDetailsService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("ReceiptDetails not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ReceiptDetails not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating ReceiptDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating ReceiptDetails: " + e.getMessage());
        }
    }

    /**
     * Delete ReceiptDetails record
     * DELETE /api/receipt-details/{id}
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting ReceiptDetails with ID: {}", id);

        try {
            boolean deleted = receiptDetailsService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("ReceiptDetails not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting ReceiptDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting ReceiptDetails: " + e.getMessage());
        }
    }

    /**
     * Get ReceiptDetails by sale master
     * GET /api/receipt-details/sale-master/{saleMasterRefId}
     */
    @GetMapping("/sale-master/{saleMasterRefId}")
    @PermitAll
    public ResponseEntity<List<ReceiptDetailsDto>> getBySaleMasterId(
            @PathVariable Integer saleMasterRefId) {
        logger.info("Fetching ReceiptDetails by sale master: {}", saleMasterRefId);
        List<ReceiptDetailsDto> records = receiptDetailsService.getBySaleMasterId(saleMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get ReceiptDetails by customer open
     * GET /api/receipt-details/customer-open/{customerOpenRefId}
     */
    @GetMapping("/customer-open/{customerOpenRefId}")
    @PermitAll
    public ResponseEntity<List<ReceiptDetailsDto>> getByCustomerOpenId(
            @PathVariable Integer customerOpenRefId) {
        logger.info("Fetching ReceiptDetails by customer open: {}", customerOpenRefId);
        List<ReceiptDetailsDto> records = receiptDetailsService.getByCustomerOpenId(customerOpenRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Count ReceiptDetails for a Receipt
     * GET /api/receipt-details/receipt/{receiptRefId}/count
     */
    @GetMapping("/receipt/{receiptRefId}/count")
    @PermitAll
    public ResponseEntity<?> countByReceiptId(
            @PathVariable Integer receiptRefId) {
        logger.info("Counting ReceiptDetails for Receipt: {}", receiptRefId);
        long count = receiptDetailsService.countByReceiptId(receiptRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Delete all ReceiptDetails for a Receipt
     * DELETE /api/receipt-details/receipt/{receiptRefId}
     */
    @DeleteMapping("/receipt/{receiptRefId}")
    @PermitAll
    public ResponseEntity<?> deleteByReceiptId(
            @PathVariable Integer receiptRefId) {
        logger.info("Deleting all ReceiptDetails for Receipt: {}", receiptRefId);

        try {
            receiptDetailsService.deleteByReceiptId(receiptRefId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            logger.error("Error deleting ReceiptDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting ReceiptDetails: " + e.getMessage());
        }
    }
}


