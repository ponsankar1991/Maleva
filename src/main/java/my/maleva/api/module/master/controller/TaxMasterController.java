package my.maleva.api.module.master.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.master.dto.TaxMasterDto;
import my.maleva.api.module.master.service.TaxMasterService;
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
 * TaxMasterController - REST Controller for TaxMaster API
 */
@RestController
@RequestMapping("/api/tax-masters")
@PermitAll
public class TaxMasterController {

    private static final Logger logger = LoggerFactory.getLogger(TaxMasterController.class);

    @Autowired
    private TaxMasterService service;

    /**
     * Get all TaxMaster records by company ID
     * GET /api/tax-masters/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<TaxMasterDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching TaxMaster for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get active TaxMaster records by company
     * GET /api/tax-masters/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<List<TaxMasterDto>> getActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active TaxMaster for company: {}", companyRefId);
        return ResponseEntity.ok(service.getActiveByCompanyRefId(companyRefId));
    }

    /**
     * Get TaxMaster by tax code
     * GET /api/tax-masters/code/{code}/company/{companyRefId}
     */
    @GetMapping("/code/{code}/company/{companyRefId}")
    public ResponseEntity<?> getByCode(@PathVariable String code, @PathVariable Integer companyRefId) {
        logger.info("Fetching TaxMaster by code: {} for company: {}", code, companyRefId);
        Optional<TaxMasterDto> record = service.getByCode(code, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get TaxMaster by description
     * GET /api/tax-masters/description/{description}/company/{companyRefId}
     */
    @GetMapping("/description/{description}/company/{companyRefId}")
    public ResponseEntity<?> getByDescription(@PathVariable String description, @PathVariable Integer companyRefId) {
        logger.info("Fetching TaxMaster by description: {} for company: {}", description, companyRefId);
        Optional<TaxMasterDto> record = service.getByDescription(description, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get TaxMaster records by tax IO type
     * GET /api/tax-masters/tax-io/{taxIO}
     */
    @GetMapping("/tax-io/{taxIO}")
    public ResponseEntity<List<TaxMasterDto>> getByTaxIO(@PathVariable Integer taxIO) {
        logger.info("Fetching TaxMaster for tax IO: {}", taxIO);
        return ResponseEntity.ok(service.getByTaxIO(taxIO));
    }

    /**
     * Get TaxMaster records by company and tax IO
     * GET /api/tax-masters/company/{companyRefId}/tax-io/{taxIO}
     */
    @GetMapping("/company/{companyRefId}/tax-io/{taxIO}")
    public ResponseEntity<List<TaxMasterDto>> getByCompanyAndTaxIO(
            @PathVariable Integer companyRefId,
            @PathVariable Integer taxIO) {
        logger.info("Fetching TaxMaster for company: {} and tax IO: {}", companyRefId, taxIO);
        return ResponseEntity.ok(service.getByCompanyAndTaxIO(companyRefId, taxIO));
    }

    /**
     * Get TaxMaster by ID
     * GET /api/tax-masters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching TaxMaster by ID: {}", id);
        Optional<TaxMasterDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new TaxMaster
     * POST /api/tax-masters
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TaxMasterDto dto) {
        logger.info("Creating new TaxMaster");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Process TaxMaster (SP_TaxMaster logic - INSERT or UPDATE with check flag)
     * POST /api/tax-masters/process?companyId=1&checkFlag=1
     */
    @PostMapping("/process")
    public ResponseEntity<?> processTaxMaster(
            @Valid @RequestBody TaxMasterDto dto,
            @RequestParam Integer companyId,
            @RequestParam(defaultValue = "0") Integer checkFlag) {
        logger.info("Processing TaxMaster with SP_TaxMaster logic for company: {} with check flag: {}", companyId, checkFlag);
        try {
            TaxMasterDto result = service.processTaxMaster(dto, companyId, checkFlag);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update TaxMaster
     * PUT /api/tax-masters/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody TaxMasterDto dto) {
        logger.info("Updating TaxMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete TaxMaster
     * DELETE /api/tax-masters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting TaxMaster with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Activate TaxMaster
     * PUT /api/tax-masters/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateTax(@PathVariable Integer id) {
        logger.info("Activating TaxMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.activateTax(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Deactivate TaxMaster
     * PUT /api/tax-masters/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateTax(@PathVariable Integer id) {
        logger.info("Deactivating TaxMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.deactivateTax(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Count TaxMaster records by company ID
     * GET /api/tax-masters/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting TaxMaster for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active TaxMaster records by company
     * GET /api/tax-masters/company/{companyRefId}/active/count
     */
    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<?> countActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting active TaxMaster for company: {}", companyRefId);
        long count = service.countActiveByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Check if TaxMaster exists by code
     * GET /api/tax-masters/code/{code}/company/{companyRefId}/exists
     */
    @GetMapping("/code/{code}/company/{companyRefId}/exists")
    public ResponseEntity<?> existsByCode(@PathVariable String code, @PathVariable Integer companyRefId) {
        logger.info("Checking if TaxMaster exists with code: {} for company: {}", code, companyRefId);
        boolean exists = service.existsByCode(code, companyRefId);
        return ResponseEntity.ok("Exists: " + exists);
    }

    /**
     * Select TaxMaster records by company (excluding deleted - Active != 2)
     * Equivalent to C# SelectTax method
     * GET /api/tax-masters/select/{companyId}
     */
    @GetMapping("/select/{companyId}")
    public ResponseEntity<ApiResponse<List<TaxMasterDto>>> selectTax(@PathVariable Integer companyId) {
        logger.info("Select Tax API called - companyId: {}", companyId);

        try {
            if (companyId == null || companyId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, "Company ID must be a valid positive integer"));
            }

            List<TaxMasterDto> taxList = service.selectTax(companyId);

            if (taxList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure(
                                HttpStatus.NOT_FOUND,
                                "No tax records found for company ID: " + companyId
                        ));
            }

            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Tax records retrieved successfully",
                            taxList
                    )
            );

        } catch (Exception e) {
            logger.error("Error while fetching tax records", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Internal server error: " + e.getMessage()
                    ));
        }
    }
}

