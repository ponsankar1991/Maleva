package my.maleva.api.module.productmaster.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.productmaster.dto.ProductMasterDto;
import my.maleva.api.module.productmaster.service.ProductMasterService;
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
 * ProductMaster REST Controller
 * Handles all RESTful API endpoints for ProductMaster operations
 * Base URL: /api/product-masters
 */
@RestController
@RequestMapping("/api/product-masters")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductMasterController {

    private static final Logger logger = LoggerFactory.getLogger(ProductMasterController.class);

    @Autowired
    private ProductMasterService productMasterService;

    /**
     * Get all ProductMaster records by company ID
     */
    @GetMapping("/company/{companyRefId}")
    @PermitAll
    public ResponseEntity<List<ProductMasterDto>> getAllByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching all ProductMaster records for company: {}", companyRefId);
        List<ProductMasterDto> records = productMasterService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get active ProductMaster records by company ID
     */
    @GetMapping("/company/{companyRefId}/active")
    @PermitAll
    public ResponseEntity<List<ProductMasterDto>> getActiveByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching active ProductMaster records for company: {}", companyRefId);
        List<ProductMasterDto> records = productMasterService.getActiveByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get ProductMaster by ID
     */
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching ProductMaster by ID: {}", id);
        Optional<ProductMasterDto> record = productMasterService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ProductMaster not found with ID: " + id);
        }
    }

    /**
     * Create new ProductMaster record
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<?> create(@Valid @RequestBody ProductMasterDto dto) {
        logger.info("Creating new ProductMaster for company: {}", dto.getCompanyRefId());

        try {
            ProductMasterDto created = productMasterService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating ProductMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating ProductMaster: " + e.getMessage());
        }
    }

    /**
     * Update ProductMaster record
     */
    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody ProductMasterDto dto) {
        logger.info("Updating ProductMaster with ID: {}", id);

        try {
            ProductMasterDto updated = productMasterService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("Error updating ProductMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating ProductMaster: " + e.getMessage());
        }
    }

    /**
     * Delete ProductMaster record
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting ProductMaster with ID: {}", id);

        boolean deleted = productMasterService.delete(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ProductMaster not found with ID: " + id);
        }
    }

    /**
     * Get ProductMaster by product code
     */
    @GetMapping("/code/{prodCode}")
    @PermitAll
    public ResponseEntity<?> getByProdCode(
            @PathVariable String prodCode,
            @RequestParam Integer companyRefId) {
        logger.info("Fetching ProductMaster by product code: {}", prodCode);
        Optional<ProductMasterDto> record = productMasterService.getByProdCode(companyRefId, prodCode);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ProductMaster not found with product code: " + prodCode);
        }
    }

    /**
     * Search ProductMaster by product name
     */
    @GetMapping("/company/{companyRefId}/search")
    @PermitAll
    public ResponseEntity<List<ProductMasterDto>> searchByProductName(
            @PathVariable Integer companyRefId,
            @RequestParam String pname) {
        logger.info("Searching ProductMaster by name: {}", pname);
        List<ProductMasterDto> records = productMasterService.searchByProductName(companyRefId, pname);
        return ResponseEntity.ok(records);
    }

    /**
     * Get ProductMaster by HSN Code
     */
    @GetMapping("/hsn/{hsnCode}")
    @PermitAll
    public ResponseEntity<List<ProductMasterDto>> getByHsnCode(@PathVariable String hsnCode) {
        logger.info("Fetching ProductMaster by HSN Code: {}", hsnCode);
        List<ProductMasterDto> records = productMasterService.getByHsnCode(hsnCode);
        return ResponseEntity.ok(records);
    }

    /**
     * Get ProductMaster by Tax Code
     */
    @GetMapping("/tax-code/{taxCode}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPRERADMIN','SUPERADMIN') or hasAnyAuthority('ROLE_ADMIN','ROLE_SUPERADMIN','ROLE_SUPERADMIN','ROLE_100','ROLE_200')")
    public ResponseEntity<List<ProductMasterDto>> getByTaxCode(@PathVariable Integer taxCode) {
        logger.info("Fetching ProductMaster by Tax Code: {}", taxCode);
        List<ProductMasterDto> records = productMasterService.getByTaxCode(taxCode);
        return ResponseEntity.ok(records);
    }

    /**
     * Get ProductMaster by UOM Code
     */
    @GetMapping("/uom-code/{uomCode}")
 @PermitAll
    public ResponseEntity<List<ProductMasterDto>> getByUomCode(@PathVariable Integer uomCode) {
        logger.info("Fetching ProductMaster by UOM Code: {}", uomCode);
        List<ProductMasterDto> records = productMasterService.getByUomCode(uomCode);
        return ResponseEntity.ok(records);
    }

    /**
     * Check if product code exists
     */
    @GetMapping("/check-code")
    @PermitAll
    public ResponseEntity<Boolean> existsByProdCode(
            @RequestParam Integer companyRefId,
            @RequestParam String prodCode) {
        logger.info("Checking if product code exists: {}", prodCode);
        boolean exists = productMasterService.existsByProdCode(companyRefId, prodCode);
        return ResponseEntity.ok(exists);
    }

    /**
     * Get ProductMaster by second product code
     */
    @GetMapping("/second-code/{secondPCode}")
    @PermitAll
    public ResponseEntity<?> getBySecondPCode(@PathVariable String secondPCode) {
        logger.info("Fetching ProductMaster by second product code: {}", secondPCode);
        Optional<ProductMasterDto> record = productMasterService.getBySecondPCode(secondPCode);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ProductMaster not found with second product code: " + secondPCode);
        }
    }

    /**
     * Get products by is product flag
     */
    @GetMapping("/company/{companyRefId}/is-product/{isProduct}")
    @PermitAll
    public ResponseEntity<List<ProductMasterDto>> getByIsProduct(
            @PathVariable Integer companyRefId,
            @PathVariable Integer isProduct) {
        logger.info("Fetching ProductMaster by is product flag: {}", isProduct);
        List<ProductMasterDto> records = productMasterService.getByIsProduct(companyRefId, isProduct);
        return ResponseEntity.ok(records);
    }

    /**
     * Count products by company
     */
    @GetMapping("/company/{companyRefId}/count")
    @PermitAll
    public ResponseEntity<Long> countByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting ProductMaster records for company: {}", companyRefId);
        Long count = productMasterService.countByCompanyId(companyRefId);
        return ResponseEntity.ok(count);
    }

    /**
     * Count active products by company
     */
    @GetMapping("/company/{companyRefId}/count-active")
    @PermitAll
    public ResponseEntity<Long> countActiveByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting active ProductMaster records for company: {}", companyRefId);
        Long count = productMasterService.countActiveByCompanyId(companyRefId);
        return ResponseEntity.ok(count);
    }

    /**
     * Activate ProductMaster record
     */
    @PostMapping("/{id}/activate")
    @PermitAll
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating ProductMaster with ID: {}", id);

        try {
            ProductMasterDto activated = productMasterService.activate(id);
            return ResponseEntity.ok(activated);
        } catch (Exception e) {
            logger.error("Error activating ProductMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating ProductMaster: " + e.getMessage());
        }
    }

    /**
     * Deactivate ProductMaster record
     */
    @PostMapping("/{id}/deactivate")
    @PermitAll
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating ProductMaster with ID: {}", id);

        try {
            ProductMasterDto deactivated = productMasterService.deactivate(id);
            return ResponseEntity.ok(deactivated);
        } catch (Exception e) {
            logger.error("Error deactivating ProductMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deactivating ProductMaster: " + e.getMessage());
        }
    }

    /**
     * Save a batch of products in one transaction.
     * POST /api/product-masters/batch?companyId=6
     *
     * Rows without an id are inserted, rows with one are updated - the shape
     * the grid screen posts when several rows were edited before saving.
     */
    @PostMapping("/batch")
    @PermitAll
    public ResponseEntity<?> saveBatch(
            @Valid @RequestBody List<ProductMasterDto> products,
            @RequestParam Integer companyId) {
        logger.info("Saving batch of {} products for company {}", products.size(), companyId);
        try {
            return ResponseEntity.ok(productMasterService.saveBatch(products, companyId));
        } catch (RuntimeException e) {
            logger.error("Batch save failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error saving products: " + e.getMessage());
        }
    }

    /**
     * Execute SP_ProductMaster stored procedure for bulk operations
     */
    @PostMapping("/bulk-import")
    @PermitAll
    public ResponseEntity<?> executeBulkImport(
            @RequestParam String detailsJson,
            @RequestParam Integer companyId,
            @RequestParam(defaultValue = "0") Integer commonCompanyDiffStock) {
        logger.info("Executing bulk import for ProductMaster with company ID: {}", companyId);

        try {
            productMasterService.executeProductMasterStoredProcedure(detailsJson, companyId, commonCompanyDiffStock);
            return ResponseEntity.ok().body("Bulk import executed successfully");
        } catch (Exception e) {
            logger.error("Error executing bulk import", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error executing bulk import: " + e.getMessage());
        }
    }
}


