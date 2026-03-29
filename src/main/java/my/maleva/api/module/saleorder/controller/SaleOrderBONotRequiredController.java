package my.maleva.api.module.saleorder.controller;

import my.maleva.api.module.saleorder.dto.SaleOrderBONotRequiredDto;
import my.maleva.api.module.saleorder.service.SaleOrderBONotRequiredService;
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
 * SaleOrderBONotRequiredController - REST Controller for SaleOrderBONotRequired API
 */
@RestController
@RequestMapping("/api/sale-order-bo-not-required")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SaleOrderBONotRequiredController {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderBONotRequiredController.class);

    @Autowired
    private SaleOrderBONotRequiredService service;

    /**
     * Get all SaleOrderBONotRequired records by SaleOrderMasterRefId
     * GET /api/sale-order-bo-not-required/sale-order/{saleOrderMasterRefId}
     */
    @GetMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<List<SaleOrderBONotRequiredDto>> getBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderBONotRequired for master: {}", saleOrderMasterRefId);
        return ResponseEntity.ok(service.getBySaleOrderMasterRefId(saleOrderMasterRefId));
    }

    /**
     * Get all SaleOrderBONotRequired records by BOTypeId
     * GET /api/sale-order-bo-not-required/bo-type/{boTypeId}
     */
    @GetMapping("/bo-type/{boTypeId}")
    public ResponseEntity<List<SaleOrderBONotRequiredDto>> getByBoTypeId(@PathVariable Integer boTypeId) {
        logger.info("Fetching SaleOrderBONotRequired for BOTypeId: {}", boTypeId);
        return ResponseEntity.ok(service.getByBoTypeId(boTypeId));
    }

    /**
     * Get SaleOrderBONotRequired by ID
     * GET /api/sale-order-bo-not-required/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleOrderBONotRequired by ID: {}", id);
        Optional<SaleOrderBONotRequiredDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SaleOrderBONotRequired
     * POST /api/sale-order-bo-not-required
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleOrderBONotRequiredDto dto) {
        logger.info("Creating new SaleOrderBONotRequired");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SaleOrderBONotRequired
     * PUT /api/sale-order-bo-not-required/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleOrderBONotRequiredDto dto) {
        logger.info("Updating SaleOrderBONotRequired with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SaleOrderBONotRequired
     * DELETE /api/sale-order-bo-not-required/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleOrderBONotRequired with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count SaleOrderBONotRequired records by SaleOrderMasterRefId
     * GET /api/sale-order-bo-not-required/count/{saleOrderMasterRefId}
     */
    @GetMapping("/count/{saleOrderMasterRefId}")
    public ResponseEntity<?> countBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Counting SaleOrderBONotRequired for master: {}", saleOrderMasterRefId);
        long count = service.countBySaleOrderMasterRefId(saleOrderMasterRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Delete all SaleOrderBONotRequired records by SaleOrderMasterRefId
     * DELETE /api/sale-order-bo-not-required/sale-order/{saleOrderMasterRefId}
     */
    @DeleteMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<?> deleteBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleOrderBONotRequired for master: {}", saleOrderMasterRefId);
        try {
            service.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}

