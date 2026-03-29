package my.maleva.api.module.saleorder.controller;

import my.maleva.api.module.saleorder.dto.SaleOrderForwardingDto;
import my.maleva.api.module.saleorder.service.SaleOrderForwardingService;
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
 * SaleOrderForwardingController - REST Controller for SaleOrderForwarding API
 */
@RestController
@RequestMapping("/api/sale-order-forwardings")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SaleOrderForwardingController {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderForwardingController.class);

    @Autowired
    private SaleOrderForwardingService service;

    /**
     * Get all SaleOrderForwarding records by SaleOrderMasterRefId
     * GET /api/sale-order-forwardings/sale-order/{saleOrderMasterRefId}
     */
    @GetMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<List<SaleOrderForwardingDto>> getBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderForwarding for master: {}", saleOrderMasterRefId);
        return ResponseEntity.ok(service.getBySaleOrderMasterRefId(saleOrderMasterRefId));
    }

    /**
     * Get SaleOrderForwarding by ID
     * GET /api/sale-order-forwardings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleOrderForwarding by ID: {}", id);
        Optional<SaleOrderForwardingDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SaleOrderForwarding
     * POST /api/sale-order-forwardings
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleOrderForwardingDto dto) {
        logger.info("Creating new SaleOrderForwarding");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SaleOrderForwarding
     * PUT /api/sale-order-forwardings/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleOrderForwardingDto dto) {
        logger.info("Updating SaleOrderForwarding with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SaleOrderForwarding
     * DELETE /api/sale-order-forwardings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleOrderForwarding with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count SaleOrderForwarding records by SaleOrderMasterRefId
     * GET /api/sale-order-forwardings/count/{saleOrderMasterRefId}
     */
    @GetMapping("/count/{saleOrderMasterRefId}")
    public ResponseEntity<?> countBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderForwarding for master: {}", saleOrderMasterRefId);
        long count = service.countBySaleOrderMasterRefId(saleOrderMasterRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Delete all SaleOrderForwarding records by SaleOrderMasterRefId
     * DELETE /api/sale-order-forwardings/sale-order/{saleOrderMasterRefId}
     */
    @DeleteMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<?> deleteBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderForwarding for master: {}", saleOrderMasterRefId);
        try {
            service.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}

