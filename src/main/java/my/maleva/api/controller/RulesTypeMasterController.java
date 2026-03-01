package my.maleva.api.controller;

import my.maleva.api.dto.RulesTypeMasterDto;
import my.maleva.api.service.RulesTypeMasterService;
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
 * RulesTypeMaster REST Controller
 * Handles all RESTful API endpoints for RulesTypeMaster operations
 * Base URL: /api/rules-type-masters
 */
@RestController
@RequestMapping("/api/rules-type-masters")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RulesTypeMasterController {

    private static final Logger logger = LoggerFactory.getLogger(RulesTypeMasterController.class);

    @Autowired
    private RulesTypeMasterService rulesTypeMasterService;

    /**
     * Get all RulesTypeMaster records by company ID
     * GET /api/rules-type-masters/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<List<RulesTypeMasterDto>> getAllByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching all RulesTypeMaster records for company: {}", companyRefId);
        List<RulesTypeMasterDto> records = rulesTypeMasterService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get active RulesTypeMaster records by company ID
     * GET /api/rules-type-masters/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<List<RulesTypeMasterDto>> getActiveByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching active RulesTypeMaster records for company: {}", companyRefId);
        List<RulesTypeMasterDto> records = rulesTypeMasterService.getActiveByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get RulesTypeMaster by ID
     * GET /api/rules-type-masters/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching RulesTypeMaster by ID: {}", id);
        Optional<RulesTypeMasterDto> record = rulesTypeMasterService.getById(id);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RulesTypeMaster not found with ID: " + id);
        }
    }

    /**
     * Create new RulesTypeMaster record
     * POST /api/rules-type-masters
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody RulesTypeMasterDto dto) {
        logger.info("Creating new RulesTypeMaster for company: {}", dto.getCompanyRefId());
        try {
            RulesTypeMasterDto created = rulesTypeMasterService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating RulesTypeMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating RulesTypeMaster: " + e.getMessage());
        }
    }

    /**
     * Update RulesTypeMaster record
     * PUT /api/rules-type-masters/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody RulesTypeMasterDto dto) {
        logger.info("Updating RulesTypeMaster with ID: {}", id);
        try {
            RulesTypeMasterDto updated = rulesTypeMasterService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("RulesTypeMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RulesTypeMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating RulesTypeMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating RulesTypeMaster: " + e.getMessage());
        }
    }

    /**
     * Delete RulesTypeMaster record
     * DELETE /api/rules-type-masters/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting RulesTypeMaster with ID: {}", id);
        try {
            boolean deleted = rulesTypeMasterService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RulesTypeMaster not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting RulesTypeMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting RulesTypeMaster: " + e.getMessage());
        }
    }

    /**
     * Get RulesTypeMaster by rule type code
     * GET /api/rules-type-masters/company/{companyRefId}/code/{ruleTypeCode}
     */
    @GetMapping("/company/{companyRefId}/code/{ruleTypeCode}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> getByRuleTypeCode(@PathVariable Integer companyRefId, @PathVariable String ruleTypeCode) {
        logger.info("Fetching RulesTypeMaster by rule type code: {}", ruleTypeCode);
        Optional<RulesTypeMasterDto> record = rulesTypeMasterService.getByRuleTypeCode(companyRefId, ruleTypeCode);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RulesTypeMaster not found with code: " + ruleTypeCode);
        }
    }

    /**
     * Get RulesTypeMaster by rule type name
     * GET /api/rules-type-masters/company/{companyRefId}/name/{ruleTypeName}
     */
    @GetMapping("/company/{companyRefId}/name/{ruleTypeName}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> getByRuleTypeName(@PathVariable Integer companyRefId, @PathVariable String ruleTypeName) {
        logger.info("Fetching RulesTypeMaster by rule type name: {}", ruleTypeName);
        Optional<RulesTypeMasterDto> record = rulesTypeMasterService.getByRuleTypeName(companyRefId, ruleTypeName);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RulesTypeMaster not found with name: " + ruleTypeName);
        }
    }

    /**
     * Count RulesTypeMaster by company
     * GET /api/rules-type-masters/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> countByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting RulesTypeMaster records for company: {}", companyRefId);
        long count = rulesTypeMasterService.countByCompanyId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active RulesTypeMaster by company
     * GET /api/rules-type-masters/company/{companyRefId}/count/active
     */
    @GetMapping("/company/{companyRefId}/count/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> countActiveByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting active RulesTypeMaster records for company: {}", companyRefId);
        long count = rulesTypeMasterService.countActiveByCompanyId(companyRefId);
        return ResponseEntity.ok("Active Total: " + count);
    }

    /**
     * Activate RulesTypeMaster record
     * POST /api/rules-type-masters/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating RulesTypeMaster with ID: {}", id);
        try {
            RulesTypeMasterDto activated = rulesTypeMasterService.activate(id);
            return ResponseEntity.ok(activated);
        } catch (RuntimeException e) {
            logger.error("RulesTypeMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RulesTypeMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error activating RulesTypeMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error activating RulesTypeMaster: " + e.getMessage());
        }
    }

    /**
     * Deactivate RulesTypeMaster record
     * POST /api/rules-type-masters/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating RulesTypeMaster with ID: {}", id);
        try {
            RulesTypeMasterDto deactivated = rulesTypeMasterService.deactivate(id);
            return ResponseEntity.ok(deactivated);
        } catch (RuntimeException e) {
            logger.error("RulesTypeMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RulesTypeMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error deactivating RulesTypeMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deactivating RulesTypeMaster: " + e.getMessage());
        }
    }
}

