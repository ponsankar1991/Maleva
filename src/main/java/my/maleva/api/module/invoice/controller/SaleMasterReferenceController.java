package my.maleva.api.module.invoice.controller;

import my.maleva.api.module.invoice.dto.SaleMasterReferenceDto;
import my.maleva.api.module.invoice.service.SaleMasterReferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Optional;

/**
 * SaleMasterReferenceController - REST Controller for SaleMasterReference API
 */
@RestController
@RequestMapping("/api/sale-master-references")
@PermitAll
public class SaleMasterReferenceController {

    private static final Logger logger = LoggerFactory.getLogger(SaleMasterReferenceController.class);

    @Autowired
    private SaleMasterReferenceService service;

    /**
     * Get all SaleMasterReference records by SaleMasterRefId
     * GET /api/sale-master-references/sale-master/{saleMasterRefId}
     */
    @GetMapping("/sale-master/{saleMasterRefId}")
    public ResponseEntity<List<SaleMasterReferenceDto>> getBySaleMasterRefId(@PathVariable Integer saleMasterRefId) {
        logger.info("Fetching SaleMasterReference for SaleMaster: {}", saleMasterRefId);
        return ResponseEntity.ok(service.getBySaleMasterRefId(saleMasterRefId));
    }

    /**
     * Get all SaleMasterReference records by SaleOrderMasterRefId
     * GET /api/sale-master-references/sale-order/{saleOrderMasterRefId}
     */
    @GetMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<List<SaleMasterReferenceDto>> getBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleMasterReference for SaleOrderMaster: {}", saleOrderMasterRefId);
        return ResponseEntity.ok(service.getBySaleOrderMasterRefId(saleOrderMasterRefId));
    }

    /**
     * Get SaleMasterReference by ID
     * GET /api/sale-master-references/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleMasterReference by ID: {}", id);
        Optional<SaleMasterReferenceDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SaleMasterReference
     * POST /api/sale-master-references
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleMasterReferenceDto dto) {
        logger.info("Creating new SaleMasterReference");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SaleMasterReference
     * PUT /api/sale-master-references/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleMasterReferenceDto dto) {
        logger.info("Updating SaleMasterReference with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SaleMasterReference
     * DELETE /api/sale-master-references/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleMasterReference with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Count SaleMasterReference records by SaleMasterRefId
     * GET /api/sale-master-references/sale-master/{saleMasterRefId}/count
     */
    @GetMapping("/sale-master/{saleMasterRefId}/count")
    public ResponseEntity<?> countBySaleMasterRefId(@PathVariable Integer saleMasterRefId) {
        logger.info("Counting SaleMasterReference for SaleMaster: {}", saleMasterRefId);
        long count = service.countBySaleMasterRefId(saleMasterRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Delete all SaleMasterReference records by SaleOrderMasterRefId
     * DELETE /api/sale-master-references/sale-order/{saleOrderMasterRefId}
     */
    @DeleteMapping("/sale-order/{saleOrderMasterRefId}")
    public ResponseEntity<?> deleteBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Deleting all SaleMasterReference for SaleOrderMaster: {}", saleOrderMasterRefId);
        try {
            service.deleteBySaleOrderMasterRefId(saleOrderMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}


