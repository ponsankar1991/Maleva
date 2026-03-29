package my.maleva.api.module.master.controller;

import my.maleva.api.module.master.dto.SymbolMasterDto;
import my.maleva.api.module.master.service.SymbolMasterService;
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
 * SymbolMasterController - REST Controller for SymbolMaster API
 */
@RestController
@RequestMapping("/api/symbol-masters")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SymbolMasterController {

    private static final Logger logger = LoggerFactory.getLogger(SymbolMasterController.class);

    @Autowired
    private SymbolMasterService service;

    /**
     * Get all SymbolMaster records by company ID
     * GET /api/symbol-masters/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<SymbolMasterDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching SymbolMaster for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get active SymbolMaster records by company
     * GET /api/symbol-masters/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<List<SymbolMasterDto>> getActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active SymbolMaster for company: {}", companyRefId);
        return ResponseEntity.ok(service.getActiveByCompanyRefId(companyRefId));
    }

    /**
     * Get SymbolMaster by symbol name
     * GET /api/symbol-masters/name/{sName}/company/{companyRefId}
     */
    @GetMapping("/name/{sName}/company/{companyRefId}")
    public ResponseEntity<?> getBySName(@PathVariable String sName, @PathVariable Integer companyRefId) {
        logger.info("Fetching SymbolMaster by name: {} for company: {}", sName, companyRefId);
        Optional<SymbolMasterDto> record = service.getBySName(sName, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get SymbolMaster by currency name
     * GET /api/symbol-masters/currency/{cName}
     */
    @GetMapping("/currency/{cName}")
    public ResponseEntity<?> getByCName(@PathVariable String cName) {
        logger.info("Fetching SymbolMaster by currency name: {}", cName);
        Optional<SymbolMasterDto> record = service.getByCName(cName);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get all SymbolMaster records by display flag
     * GET /api/symbol-masters/flag/{dFlag}
     */
    @GetMapping("/flag/{dFlag}")
    public ResponseEntity<List<SymbolMasterDto>> getByDFlag(@PathVariable Integer dFlag) {
        logger.info("Fetching SymbolMaster for display flag: {}", dFlag);
        return ResponseEntity.ok(service.getByDFlag(dFlag));
    }

    /**
     * Get all SymbolMaster records by QNE ID
     * GET /api/symbol-masters/qne/{qneId}
     */
    @GetMapping("/qne/{qneId}")
    public ResponseEntity<List<SymbolMasterDto>> getByQneId(@PathVariable Integer qneId) {
        logger.info("Fetching SymbolMaster for QNE ID: {}", qneId);
        return ResponseEntity.ok(service.getByQneId(qneId));
    }

    /**
     * Get SymbolMaster by ID
     * GET /api/symbol-masters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SymbolMaster by ID: {}", id);
        Optional<SymbolMasterDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SymbolMaster
     * POST /api/symbol-masters
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SymbolMasterDto dto) {
        logger.info("Creating new SymbolMaster");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SymbolMaster
     * PUT /api/symbol-masters/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SymbolMasterDto dto) {
        logger.info("Updating SymbolMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SymbolMaster
     * DELETE /api/symbol-masters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SymbolMaster with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Activate SymbolMaster
     * PUT /api/symbol-masters/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateSymbol(@PathVariable Integer id) {
        logger.info("Activating SymbolMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.activateSymbol(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Deactivate SymbolMaster
     * PUT /api/symbol-masters/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateSymbol(@PathVariable Integer id) {
        logger.info("Deactivating SymbolMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.deactivateSymbol(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Count SymbolMaster records by company ID
     * GET /api/symbol-masters/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting SymbolMaster for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active SymbolMaster records by company
     * GET /api/symbol-masters/company/{companyRefId}/active/count
     */
    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<?> countActiveByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting active SymbolMaster for company: {}", companyRefId);
        long count = service.countActiveByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Check if SymbolMaster exists by name
     * GET /api/symbol-masters/name/{sName}/company/{companyRefId}/exists
     */
    @GetMapping("/name/{sName}/company/{companyRefId}/exists")
    public ResponseEntity<?> existsBySName(@PathVariable String sName, @PathVariable Integer companyRefId) {
        logger.info("Checking if SymbolMaster exists with name: {} for company: {}", sName, companyRefId);
        boolean exists = service.existsBySName(sName, companyRefId);
        return ResponseEntity.ok("Exists: " + exists);
    }

    /**
     * Process Symbol (SP_Symbol logic - INSERT or UPDATE with check flag)
     * POST /api/symbol-masters/process?companyId=1&checkFlag=1
     *
     * checkFlag = 0: Always insert if id = 0, update if id > 0
     * checkFlag = 1: Check if symbol already exists before insert
     */
    @PostMapping("/process")
    public ResponseEntity<?> processSymbol(
            @Valid @RequestBody SymbolMasterDto dto,
            @RequestParam Integer companyId,
            @RequestParam(defaultValue = "0") Integer checkFlag) {
        logger.info("Processing Symbol with SP_Symbol logic for company: {} with check flag: {}", companyId, checkFlag);
        try {
            SymbolMasterDto result = service.processSymbol(dto, companyId, checkFlag);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}

