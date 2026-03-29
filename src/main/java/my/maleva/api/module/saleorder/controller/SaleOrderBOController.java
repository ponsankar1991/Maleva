package my.maleva.api.module.saleorder.controller;

import my.maleva.api.module.saleorder.dto.SaleOrderBODto;
import my.maleva.api.module.saleorder.service.SaleOrderBOService;
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
 * SaleOrderBOController - REST Controller for SaleOrderBO API
 */
@RestController
@RequestMapping("/api/sale-order-bo")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SaleOrderBOController {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderBOController.class);

    @Autowired
    private SaleOrderBOService service;

    /**
     * Get all SaleOrderBO records by SaleOrderMasterRefId
     * GET /api/sale-order-bo/sale-order/{saleOrderMasterRefId}
     */
    @GetMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<List<SaleOrderBODto>> getBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderBO for master: {}", saleOrderMasterRefId);
        return ResponseEntity.ok(service.getBySaleOrderMasterRefId(saleOrderMasterRefId));
    }

    /**
     * Get all SaleOrderBO records by BOTypeId
     * GET /api/sale-order-bo/bo-type/{boTypeId}
     */
    @GetMapping("/bo-type/{boTypeId}")
    public ResponseEntity<List<SaleOrderBODto>> getByBoTypeId(@PathVariable Integer boTypeId) {
        logger.info("Fetching SaleOrderBO for BOTypeId: {}", boTypeId);
        return ResponseEntity.ok(service.getByBoTypeId(boTypeId));
    }

    /**
     * Get all SaleOrderBO records by Status
     * GET /api/sale-order-bo/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SaleOrderBODto>> getByStatus(@PathVariable Integer status) {
        logger.info("Fetching SaleOrderBO for status: {}", status);
        return ResponseEntity.ok(service.getByStatus(status));
    }

    /**
     * Get SaleOrderBO by ID
     * GET /api/sale-order-bo/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleOrderBO by ID: {}", id);
        Optional<SaleOrderBODto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SaleOrderBO
     * POST /api/sale-order-bo
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleOrderBODto dto) {
        logger.info("Creating new SaleOrderBO");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SaleOrderBO
     * PUT /api/sale-order-bo/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleOrderBODto dto) {
        logger.info("Updating SaleOrderBO with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SaleOrderBO
     * DELETE /api/sale-order-bo/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleOrderBO with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count SaleOrderBO records by SaleOrderMasterRefId
     * GET /api/sale-order-bo/count/{saleOrderMasterRefId}
     */
    @GetMapping("/count/{saleOrderMasterRefId}")
    public ResponseEntity<?> countBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderBO for master: {}", saleOrderMasterRefId);
        long count = service.countBySaleOrderMasterRefId(saleOrderMasterRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Delete all SaleOrderBO records by SaleOrderMasterRefId
     * DELETE /api/sale-order-bo/sale-order/{saleOrderMasterRefId}
     */
    @DeleteMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<?> deleteBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderBO for master: {}", saleOrderMasterRefId);
        try {
            service.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}

