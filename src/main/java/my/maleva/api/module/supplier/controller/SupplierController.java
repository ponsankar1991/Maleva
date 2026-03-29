package my.maleva.api.module.supplier.controller;

import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.service.SupplierService;
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
 * SupplierController - REST Controller for Supplier API
 */
@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    @Autowired
    private SupplierService service;

    /**
     * Get all Supplier records by company ID
     * GET /api/suppliers/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<SupplierDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching Supplier for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get active Supplier records by company
     * GET /api/suppliers/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<List<SupplierDto>> getActiveByCompany(@PathVariable Integer companyRefId) {
        logger.info("Fetching active Supplier for company: {}", companyRefId);
        return ResponseEntity.ok(service.getActiveByCompany(companyRefId));
    }

    /**
     * Get Supplier by name
     * GET /api/suppliers/name/{supplierName}
     */
    @GetMapping("/name/{supplierName}")
    public ResponseEntity<?> getBySupplierName(@PathVariable String supplierName) {
        logger.info("Fetching Supplier by name: {}", supplierName);
        Optional<SupplierDto> record = service.getBySupplierName(supplierName);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get Supplier by C Number
     * GET /api/suppliers/c-number/{cNumber}/company/{companyRefId}
     */
    @GetMapping("/c-number/{cNumber}/company/{companyRefId}")
    public ResponseEntity<?> getByCNumber(@PathVariable Integer cNumber, @PathVariable Integer companyRefId) {
        logger.info("Fetching Supplier by C Number: {} for company: {}", cNumber, companyRefId);
        Optional<SupplierDto> record = service.getByCNumber(cNumber, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get Supplier records by type
     * GET /api/suppliers/type/{supplierType}
     */
    @GetMapping("/type/{supplierType}")
    public ResponseEntity<List<SupplierDto>> getBySupplierType(@PathVariable String supplierType) {
        logger.info("Fetching Supplier for type: {}", supplierType);
        return ResponseEntity.ok(service.getBySupplierType(supplierType));
    }

    /**
     * Get Supplier records by country
     * GET /api/suppliers/country/{country}
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<List<SupplierDto>> getByCountry(@PathVariable String country) {
        logger.info("Fetching Supplier for country: {}", country);
        return ResponseEntity.ok(service.getByCountry(country));
    }

    /**
     * Get Supplier records by city
     * GET /api/suppliers/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<SupplierDto>> getByCity(@PathVariable String city) {
        logger.info("Fetching Supplier for city: {}", city);
        return ResponseEntity.ok(service.getByCity(city));
    }

    /**
     * Get Supplier by email
     * GET /api/suppliers/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email) {
        logger.info("Fetching Supplier by email: {}", email);
        Optional<SupplierDto> record = service.getByEmail(email);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get Supplier by GST No
     * GET /api/suppliers/gst/{gstNo}
     */
    @GetMapping("/gst/{gstNo}")
    public ResponseEntity<?> getByGstNo(@PathVariable String gstNo) {
        logger.info("Fetching Supplier by GST No: {}", gstNo);
        Optional<SupplierDto> record = service.getByGstNo(gstNo);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get Supplier by ID
     * GET /api/suppliers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching Supplier by ID: {}", id);
        Optional<SupplierDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new Supplier
     * POST /api/suppliers
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SupplierDto dto) {
        logger.info("Creating new Supplier");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update Supplier
     * PUT /api/suppliers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SupplierDto dto) {
        logger.info("Updating Supplier with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete Supplier
     * DELETE /api/suppliers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting Supplier with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Activate Supplier
     * PUT /api/suppliers/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateSupplier(@PathVariable Integer id) {
        logger.info("Activating Supplier with ID: {}", id);
        try {
            return ResponseEntity.ok(service.activateSupplier(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Deactivate Supplier
     * PUT /api/suppliers/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateSupplier(@PathVariable Integer id) {
        logger.info("Deactivating Supplier with ID: {}", id);
        try {
            return ResponseEntity.ok(service.deactivateSupplier(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Count Supplier records by company ID
     * GET /api/suppliers/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting Supplier for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active Supplier records by company
     * GET /api/suppliers/company/{companyRefId}/active/count
     */
    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<?> countActiveByCompany(@PathVariable Integer companyRefId) {
        logger.info("Counting active Supplier for company: {}", companyRefId);
        long count = service.countActiveByCompany(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Check if Supplier exists by name
     * GET /api/suppliers/name/{supplierName}/exists
     */
    @GetMapping("/name/{supplierName}/exists")
    public ResponseEntity<?> existsBySupplierName(@PathVariable String supplierName) {
        logger.info("Checking if Supplier exists with name: {}", supplierName);
        boolean exists = service.existsBySupplierName(supplierName);
        return ResponseEntity.ok("Exists: " + exists);
    }

    /**
     * Process Supplier Batch (SP_Supplier logic - INSERT or UPDATE)
     * POST /api/suppliers/process
     */
    @PostMapping("/process")
    public ResponseEntity<?> processSupplierBatch(@Valid @RequestBody SupplierDto dto) {
        logger.info("Processing Supplier batch with SP_Supplier logic");
        try {
            SupplierDto result = service.processSupplierBatch(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}


