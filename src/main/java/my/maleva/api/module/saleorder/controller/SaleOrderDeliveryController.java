package my.maleva.api.module.saleorder.controller;

import my.maleva.api.module.saleorder.dto.SaleOrderDeliveryDto;
import my.maleva.api.module.saleorder.service.SaleOrderDeliveryService;
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
 * SaleOrderDeliveryController - REST Controller for SaleOrderDelivery API
 */
@RestController
@RequestMapping("/api/sale-order-deliveries")
@PermitAll
public class SaleOrderDeliveryController {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderDeliveryController.class);

    @Autowired
    private SaleOrderDeliveryService service;

    /**
     * Get all SaleOrderDelivery records by SaleOrderMasterRefId
     * GET /api/sale-order-deliveries/sale-order/{saleOrderMasterRefId}
     */
    @GetMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<List<SaleOrderDeliveryDto>> getBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderDelivery for master: {}", saleOrderMasterRefId);
        return ResponseEntity.ok(service.getBySaleOrderMasterRefId(saleOrderMasterRefId));
    }

    /**
     * Get SaleOrderDelivery by ID
     * GET /api/sale-order-deliveries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleOrderDelivery by ID: {}", id);
        Optional<SaleOrderDeliveryDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SaleOrderDelivery
     * POST /api/sale-order-deliveries
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleOrderDeliveryDto dto) {
        logger.info("Creating new SaleOrderDelivery");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SaleOrderDelivery
     * PUT /api/sale-order-deliveries/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleOrderDeliveryDto dto) {
        logger.info("Updating SaleOrderDelivery with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }
    /**
     * Delete SaleOrderDelivery
     * DELETE /api/sale-order-deliveries/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleOrderDelivery with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count SaleOrderDelivery records by SaleOrderMasterRefId
     * GET /api/sale-order-deliveries/count/{saleOrderMasterRefId}
     */
    @GetMapping("/count/{saleOrderMasterRefId}")
    public ResponseEntity<?> countBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderDelivery for master: {}", saleOrderMasterRefId);
        long count = service.countBySaleOrderMasterRefId(saleOrderMasterRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Delete all SaleOrderDelivery records by SaleOrderMasterRefId
     * DELETE /api/sale-order-deliveries/sale-order/{saleOrderMasterRefId}
     */
    @DeleteMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<?> deleteBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderDelivery for master: {}", saleOrderMasterRefId);
        try {
            service.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}


