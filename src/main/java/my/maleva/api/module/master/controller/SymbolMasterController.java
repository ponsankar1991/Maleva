package my.maleva.api.module.master.controller;

import my.maleva.api.module.master.dto.SymbolMasterDto;
import my.maleva.api.module.master.service.SymbolMasterService;
import my.maleva.api.common.dto.ApiResponse;
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
 * SymbolMasterController - REST Controller for SymbolMaster API
 */
@RestController
@RequestMapping("/api/symbol-masters")
@PermitAll
public class SymbolMasterController {

    private static final Logger logger = LoggerFactory.getLogger(SymbolMasterController.class);

    @Autowired
    private SymbolMasterService service;

    /**
     * Get all SymbolMaster records by company ID
     * GET /api/symbol-masters/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<ApiResponse<List<SymbolMasterDto>>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching SymbolMaster for company: {}", companyRefId);
        return ResponseEntity.ok(ApiResponse.success(service.getByCompanyRefId(companyRefId), "Symbols retrieved successfully"));
    }

    /**
     * Get active SymbolMaster records by company
     * GET /api/symbol-masters/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<ApiResponse<List<SymbolMasterDto>>> getActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active SymbolMaster for company: {}", companyRefId);
        return ResponseEntity.ok(ApiResponse.success(service.getActiveByCompanyRefId(companyRefId), "Active symbols retrieved successfully"));
    }

    /**
     * Select symbols for company (equivalent to .NET SelectSymbol)
     * GET /api/symbol-masters/select/company/{companyRefId}
     *
     * Returns all non-deleted symbols (Active != 2)
     * Used for dropdowns, selection lists, and UI displays
     */
    @GetMapping("/select/company/{companyRefId}")
    public ResponseEntity<ApiResponse<List<SymbolMasterDto>>> selectSymbol(@PathVariable Integer companyRefId) {
        logger.info("Selecting SymbolMaster for company: {}", companyRefId);
        List<SymbolMasterDto> result = service.selectSymbol(companyRefId);
        logger.debug("✓ Found {} non-deleted symbols for company: {}", result.size(), companyRefId);
        return ResponseEntity.ok(ApiResponse.success(result, "Symbols retrieved successfully"));
    }

    /**
     * Get SymbolMaster by symbol name
     * GET /api/symbol-masters/name/{sName}/company/{companyRefId}
     */
    @GetMapping("/name/{sName}/company/{companyRefId}")
    public ResponseEntity<ApiResponse<?>> getBySName(@PathVariable String sName, @PathVariable Integer companyRefId) {
        logger.info("Fetching SymbolMaster by name: {} for company: {}", sName, companyRefId);
        Optional<SymbolMasterDto> record = service.getBySName(sName, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(ApiResponse.success(record.get(), "Symbol found")) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("SymbolMaster not found", HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Get SymbolMaster by currency name
     * GET /api/symbol-masters/currency/{cName}
     */
    @GetMapping("/currency/{cName}")
    public ResponseEntity<ApiResponse<?>> getByCName(@PathVariable String cName) {
        logger.info("Fetching SymbolMaster by currency name: {}", cName);
        Optional<SymbolMasterDto> record = service.getByCName(cName);
        return record.isPresent() ? ResponseEntity.ok(ApiResponse.success(record.get(), "Symbol found")) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("SymbolMaster not found", HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Get all SymbolMaster records by display flag
     * GET /api/symbol-masters/flag/{dFlag}
     */
    @GetMapping("/flag/{dFlag}")
    public ResponseEntity<ApiResponse<List<SymbolMasterDto>>> getByDFlag(@PathVariable Integer dFlag) {
        logger.info("Fetching SymbolMaster for display flag: {}", dFlag);
        return ResponseEntity.ok(ApiResponse.success(service.getByDFlag(dFlag), "Symbols retrieved successfully"));
    }

    /**
     * Get all SymbolMaster records by QNE ID
     * GET /api/symbol-masters/qne/{qneId}
     */
    @GetMapping("/qne/{qneId}")
    public ResponseEntity<ApiResponse<List<SymbolMasterDto>>> getByQneId(@PathVariable Integer qneId) {
        logger.info("Fetching SymbolMaster for QNE ID: {}", qneId);
        return ResponseEntity.ok(ApiResponse.success(service.getByQneId(qneId), "Symbols retrieved successfully"));
    }

    /**
     * Get SymbolMaster by ID
     * GET /api/symbol-masters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Integer id) {
        logger.info("Fetching SymbolMaster by ID: {}", id);
        Optional<SymbolMasterDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(ApiResponse.success(record.get(), "Symbol found")) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("SymbolMaster not found", HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Create new SymbolMaster
     * POST /api/symbol-masters
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody SymbolMasterDto dto) {
        logger.info("Creating new SymbolMaster");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(dto), "SymbolMaster created successfully"));
        } catch (RuntimeException e) {
            logger.error("Error creating SymbolMaster: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Update SymbolMaster
     * PUT /api/symbol-masters/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Integer id, @Valid @RequestBody SymbolMasterDto dto) {
        logger.info("Updating SymbolMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(ApiResponse.success(service.update(id, dto), "SymbolMaster updated successfully"));
        } catch (RuntimeException e) {
            logger.error("Error updating SymbolMaster: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Delete SymbolMaster
     * DELETE /api/symbol-masters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Integer id) {
        logger.info("Deleting SymbolMaster with ID: {}", id);
        return service.delete(id) ? ResponseEntity.ok(ApiResponse.success(null, "SymbolMaster deleted successfully")) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("SymbolMaster not found", HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Activate SymbolMaster
     * PUT /api/symbol-masters/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<?>> activateSymbol(@PathVariable Integer id) {
        logger.info("Activating SymbolMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(ApiResponse.success(service.activateSymbol(id), "SymbolMaster activated successfully"));
        } catch (RuntimeException e) {
            logger.error("Error activating SymbolMaster: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Deactivate SymbolMaster
     * PUT /api/symbol-masters/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<?>> deactivateSymbol(@PathVariable Integer id) {
        logger.info("Deactivating SymbolMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(ApiResponse.success(service.deactivateSymbol(id), "SymbolMaster deactivated successfully"));
        } catch (RuntimeException e) {
            logger.error("Error deactivating SymbolMaster: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Count SymbolMaster records by company ID
     * GET /api/symbol-masters/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<ApiResponse<?>> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting SymbolMaster for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok(ApiResponse.success(count, "Total count: " + count));
    }

    /**
     * Count active SymbolMaster records by company
     * GET /api/symbol-masters/company/{companyRefId}/active/count
     */
    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<ApiResponse<?>> countActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting active SymbolMaster for company: {}", companyRefId);
        long count = service.countActiveByCompanyRefId(companyRefId);
        return ResponseEntity.ok(ApiResponse.success(count, "Total active count: " + count));
    }

    /**
     * Check if SymbolMaster exists by name
     * GET /api/symbol-masters/name/{sName}/company/{companyRefId}/exists
     */
    @GetMapping("/name/{sName}/company/{companyRefId}/exists")
    public ResponseEntity<ApiResponse<?>> existsBySName(@PathVariable String sName, @PathVariable Integer companyRefId) {
        logger.info("Checking if SymbolMaster exists with name: {} for company: {}", sName, companyRefId);
        boolean exists = service.existsBySName(sName, companyRefId);
        return ResponseEntity.ok(ApiResponse.success(exists, exists ? "Symbol exists" : "Symbol does not exist"));
    }

    /**
     * Process Symbol (SP_Symbol logic - INSERT or UPDATE with check flag)
     * POST /api/symbol-masters/process?companyId=1&checkFlag=1
     *
     * checkFlag = 0: Always insert if id = 0, update if id > 0
     * checkFlag = 1: Check if symbol already exists before insert
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<?>> processSymbol(
            @Valid @RequestBody SymbolMasterDto dto,
            @RequestParam Integer companyId,
            @RequestParam(defaultValue = "0") Integer checkFlag) {
        logger.info("Processing Symbol with SP_Symbol logic for company: {} with check flag: {}", companyId, checkFlag);
        try {
            SymbolMasterDto result = service.processSymbol(dto, companyId, checkFlag);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Symbol processed successfully"));
        } catch (RuntimeException e) {
            logger.error("Error processing Symbol: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}


