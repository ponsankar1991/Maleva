package my.maleva.api.controller;

import my.maleva.api.dto.SubExpenseMasterDto;
import my.maleva.api.service.SubExpenseMasterService;
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
 * SubExpenseMasterController - REST Controller for SubExpenseMaster API
 */
@RestController
@RequestMapping("/api/sub-expense-masters")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
public class SubExpenseMasterController {

    private static final Logger logger = LoggerFactory.getLogger(SubExpenseMasterController.class);

    @Autowired
    private SubExpenseMasterService service;

    /**
     * Get all SubExpenseMaster records by company ID
     * GET /api/sub-expense-masters/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<SubExpenseMasterDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching SubExpenseMaster for company: {}", companyRefId);
        return ResponseEntity.ok(service.getByCompanyRefId(companyRefId));
    }

    /**
     * Get active SubExpenseMaster records by company
     * GET /api/sub-expense-masters/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<List<SubExpenseMasterDto>> getActiveByCompany(@PathVariable Integer companyRefId) {
        logger.info("Fetching active SubExpenseMaster for company: {}", companyRefId);
        return ResponseEntity.ok(service.getActiveByCompany(companyRefId));
    }

    /**
     * Get all SubExpenseMaster records by expense master ID
     * GET /api/sub-expense-masters/expense-master/{expenseMasterRefId}
     */
    @GetMapping("/expense-master/{expenseMasterRefId}")
    public ResponseEntity<List<SubExpenseMasterDto>> getByExpenseMasterRefId(@PathVariable Integer expenseMasterRefId) {
        logger.info("Fetching SubExpenseMaster for expense master: {}", expenseMasterRefId);
        return ResponseEntity.ok(service.getByExpenseMasterRefId(expenseMasterRefId));
    }

    /**
     * Get SubExpenseMaster records by company and expense master
     * GET /api/sub-expense-masters/company/{companyRefId}/expense-master/{expenseMasterRefId}
     */
    @GetMapping("/company/{companyRefId}/expense-master/{expenseMasterRefId}")
    public ResponseEntity<List<SubExpenseMasterDto>> getByCompanyAndExpenseMaster(
            @PathVariable Integer companyRefId,
            @PathVariable Integer expenseMasterRefId) {
        logger.info("Fetching SubExpenseMaster for company: {} and expense master: {}", companyRefId, expenseMasterRefId);
        return ResponseEntity.ok(service.getByCompanyAndExpenseMaster(companyRefId, expenseMasterRefId));
    }

    /**
     * Get SubExpenseMaster records by account reference
     * GET /api/sub-expense-masters/account/{accountRefid}
     */
    @GetMapping("/account/{accountRefid}")
    public ResponseEntity<List<SubExpenseMasterDto>> getByAccountRefid(@PathVariable Integer accountRefid) {
        logger.info("Fetching SubExpenseMaster for account: {}", accountRefid);
        return ResponseEntity.ok(service.getByAccountRefid(accountRefid));
    }

    /**
     * Get SubExpenseMaster records by GL account reference
     * GET /api/sub-expense-masters/gl-account/{glAccountRefId}
     */
    @GetMapping("/gl-account/{glAccountRefId}")
    public ResponseEntity<List<SubExpenseMasterDto>> getByGlAccountRefId(@PathVariable Integer glAccountRefId) {
        logger.info("Fetching SubExpenseMaster for GL account: {}", glAccountRefId);
        return ResponseEntity.ok(service.getByGlAccountRefId(glAccountRefId));
    }

    /**
     * Get SubExpenseMaster by description and company
     * GET /api/sub-expense-masters/description/{description}/company/{companyRefId}
     */
    @GetMapping("/description/{description}/company/{companyRefId}")
    public ResponseEntity<?> getByDescriptionAndCompany(@PathVariable String description, @PathVariable Integer companyRefId) {
        logger.info("Fetching SubExpenseMaster by description: {} for company: {}", description, companyRefId);
        Optional<SubExpenseMasterDto> record = service.getByDescriptionAndCompany(description, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Get SubExpenseMaster by ID
     * GET /api/sub-expense-masters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SubExpenseMaster by ID: {}", id);
        Optional<SubExpenseMasterDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Create new SubExpenseMaster
     * POST /api/sub-expense-masters
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SubExpenseMasterDto dto) {
        logger.info("Creating new SubExpenseMaster");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Process SubExpenseMaster (SP_SubExpense logic - INSERT or UPDATE)
     * POST /api/sub-expense-masters/process?companyId=1
     */
    @PostMapping("/process")
    public ResponseEntity<?> processSubExpense(
            @Valid @RequestBody SubExpenseMasterDto dto,
            @RequestParam Integer companyId) {
        logger.info("Processing SubExpenseMaster with SP_SubExpense logic for company: {}", companyId);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.processSubExpense(dto, companyId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    /**
     * Update SubExpenseMaster
     * PUT /api/sub-expense-masters/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SubExpenseMasterDto dto) {
        logger.info("Updating SubExpenseMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Delete SubExpenseMaster
     * DELETE /api/sub-expense-masters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SubExpenseMaster with ID: {}", id);
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    /**
     * Activate SubExpenseMaster
     * PUT /api/sub-expense-masters/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateSubExpense(@PathVariable Integer id) {
        logger.info("Activating SubExpenseMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.activateSubExpense(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Deactivate SubExpenseMaster
     * PUT /api/sub-expense-masters/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateSubExpense(@PathVariable Integer id) {
        logger.info("Deactivating SubExpenseMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(service.deactivateSubExpense(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    /**
     * Count SubExpenseMaster records by company ID
     * GET /api/sub-expense-masters/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Counting SubExpenseMaster for company: {}", companyRefId);
        long count = service.countByCompanyRefId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active SubExpenseMaster records by company
     * GET /api/sub-expense-masters/company/{companyRefId}/active/count
     */
    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<?> countActiveByCompany(@PathVariable Integer companyRefId) {
        logger.info("Counting active SubExpenseMaster for company: {}", companyRefId);
        long count = service.countActiveByCompany(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }
}

