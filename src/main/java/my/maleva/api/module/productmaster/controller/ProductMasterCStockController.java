package my.maleva.api.module.productmaster.controller;

import my.maleva.api.module.productmaster.dto.ProductMasterCStockDto;
import my.maleva.api.module.productmaster.service.ProductMasterCStockService;
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
 * ProductMasterCStock REST Controller
 * Handles all RESTful API endpoints for ProductMasterCStock operations
 * Base URL: /api/product-cstocks
 */
@RestController
@RequestMapping("/api/product-cstocks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductMasterCStockController {

    private static final Logger logger = LoggerFactory.getLogger(ProductMasterCStockController.class);

    @Autowired
    private ProductMasterCStockService cstockService;

    /**
     * Get all CStock records by company ID
     */
    @GetMapping("/company/{companyRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<ProductMasterCStockDto>> getAllByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching all CStock records for company: {}", companyRefId);
        List<ProductMasterCStockDto> records = cstockService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get all CStock records by product ID
     */
    @GetMapping("/product/{productRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<ProductMasterCStockDto>> getAllByProductId(
            @PathVariable Integer productRefId) {
        logger.info("Fetching all CStock records for product: {}", productRefId);
        List<ProductMasterCStockDto> records = cstockService.getAllByProductId(productRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get CStock by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching CStock by ID: {}", id);
        Optional<ProductMasterCStockDto> record = cstockService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("CStock not found with ID: " + id);
        }
    }

    /**
     * Get CStock by company and product
     */
    @GetMapping("/company/{companyRefId}/product/{productRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<ProductMasterCStockDto>> getByCompanyAndProduct(
            @PathVariable Integer companyRefId,
            @PathVariable Integer productRefId) {
        logger.info("Fetching CStock for company: {} and product: {}", companyRefId, productRefId);
        List<ProductMasterCStockDto> records = cstockService.getByCompanyAndProduct(companyRefId, productRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Create new CStock record
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody ProductMasterCStockDto dto) {
        logger.info("Creating new CStock for product: {}", dto.getProductRefId());

        try {
            ProductMasterCStockDto created = cstockService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating CStock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating CStock: " + e.getMessage());
        }
    }

    /**
     * Update CStock record
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody ProductMasterCStockDto dto) {
        logger.info("Updating CStock with ID: {}", id);

        try {
            ProductMasterCStockDto updated = cstockService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating CStock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating CStock: " + e.getMessage());
        }
    }

    /**
     * Delete CStock record
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting CStock with ID: {}", id);

        boolean deleted = cstockService.delete(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("CStock not found with ID: " + id);
        }
    }

    /**
     * Delete all CStock records by product ID
     */
    @DeleteMapping("/product/{productRefId}/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> deleteByProductId(@PathVariable Integer productRefId) {
        logger.info("Deleting all CStock records for product: {}", productRefId);

        try {
            cstockService.deleteByProductId(productRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting CStock records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting CStock records: " + e.getMessage());
        }
    }

    /**
     * Count CStock records by product
     */
    @GetMapping("/product/{productRefId}/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<Long> countByProductId(@PathVariable Integer productRefId) {
        logger.info("Counting CStock records for product: {}", productRefId);
        Long count = cstockService.countByProductId(productRefId);
        return ResponseEntity.ok(count);
    }

    /**
     * Count CStock records by company
     */
    @GetMapping("/company/{companyRefId}/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<Long> countByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting CStock records for company: {}", companyRefId);
        Long count = cstockService.countByCompanyId(companyRefId);
        return ResponseEntity.ok(count);
    }

    /**
     * Update CStock value
     */
    @PutMapping("/{id}/update-stock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> updateCStock(
            @PathVariable Integer id,
            @RequestParam Double newCStock) {
        logger.info("Updating CStock value for record: {}", id);

        try {
            ProductMasterCStockDto updated = cstockService.updateCStock(id, newCStock);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating CStock value", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating CStock value: " + e.getMessage());
        }
    }
}

