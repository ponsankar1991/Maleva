package my.maleva.api.module.salecreditmaster.controller;

import my.maleva.api.module.salecreditmaster.dto.SaleCreditKnockOffDto;
import my.maleva.api.module.salecreditmaster.service.SaleCreditKnockOffService;
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
 * SaleCreditKnockOffController
 * REST Controller for SaleCreditKnockOff API
 */
@RestController
@RequestMapping("/api/sale-credit-knock-offs")
@PermitAll
public class SaleCreditKnockOffController {

    private static final Logger logger = LoggerFactory.getLogger(SaleCreditKnockOffController.class);

    @Autowired
    private SaleCreditKnockOffService saleCreditKnockOffService;

    /**
     * Get all SaleCreditKnockOff records by Sale Credit Master Reference ID
     * GET /api/sale-credit-knock-offs/sale-credit-master/{saleCreditMasterRefId}
     */
    @GetMapping("/sale-credit-master/{saleCreditMasterRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditKnockOffDto>> getBySaleCreditMasterRefId(@PathVariable Integer saleCreditMasterRefId) {
        logger.info("Fetching SaleCreditKnockOff records by Sale Credit Master Reference ID: {}", saleCreditMasterRefId);
        List<SaleCreditKnockOffDto> records = saleCreditKnockOffService.getBySaleCreditMasterRefId(saleCreditMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditKnockOff by ID
     * GET /api/sale-credit-knock-offs/{id}
     */
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleCreditKnockOff by ID: {}", id);
        Optional<SaleCreditKnockOffDto> record = saleCreditKnockOffService.getById(id);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditKnockOff not found with ID: " + id);
    }

    /**
     * Create SaleCreditKnockOff record
     * POST /api/sale-credit-knock-offs
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<?> create(@Valid @RequestBody SaleCreditKnockOffDto dto) {
        logger.info("Creating new SaleCreditKnockOff for Sale Credit Master: {}", dto.getSaleCreditMasterRefId());
        try {
            SaleCreditKnockOffDto created = saleCreditKnockOffService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating SaleCreditKnockOff: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating SaleCreditKnockOff: " + e.getMessage());
        }
    }

    /**
     * Update SaleCreditKnockOff record
     * PUT /api/sale-credit-knock-offs/{id}
     */
    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleCreditKnockOffDto dto) {
        logger.info("Updating SaleCreditKnockOff with ID: {}", id);
        try {
            SaleCreditKnockOffDto updated = saleCreditKnockOffService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("SaleCreditKnockOff not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditKnockOff not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating SaleCreditKnockOff", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating SaleCreditKnockOff: " + e.getMessage());
        }
    }

    /**
     * Delete SaleCreditKnockOff record
     * DELETE /api/sale-credit-knock-offs/{id}
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleCreditKnockOff with ID: {}", id);
        boolean deleted = saleCreditKnockOffService.delete(id);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleCreditKnockOff not found with ID: " + id);
    }

    /**
     * Get SaleCreditKnockOff records by company ID
     * GET /api/sale-credit-knock-offs/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditKnockOffDto>> getByCompanyRefId(@PathVariable Integer companyRefId) {
        logger.info("Fetching SaleCreditKnockOff records by company ID: {}", companyRefId);
        List<SaleCreditKnockOffDto> records = saleCreditKnockOffService.getByCompanyRefId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditKnockOff records by Sale Master Reference ID
     * GET /api/sale-credit-knock-offs/sale-master/{saleMasterRefId}
     */
    @GetMapping("/sale-master/{saleMasterRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditKnockOffDto>> getBySaleMasterRefId(@PathVariable Integer saleMasterRefId) {
        logger.info("Fetching SaleCreditKnockOff records by Sale Master Reference ID: {}", saleMasterRefId);
        List<SaleCreditKnockOffDto> records = saleCreditKnockOffService.getBySaleMasterRefId(saleMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get SaleCreditKnockOff records by customer ID
     * GET /api/sale-credit-knock-offs/customer/{customerOpenRefId}
     */
    @GetMapping("/customer/{customerOpenRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditKnockOffDto>> getByCustomerOpenRefId(@PathVariable Integer customerOpenRefId) {
        logger.info("Fetching SaleCreditKnockOff records by customer ID: {}", customerOpenRefId);
        List<SaleCreditKnockOffDto> records = saleCreditKnockOffService.getByCustomerOpenRefId(customerOpenRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Count knock-off records by Sale Credit Master Reference ID
     * GET /api/sale-credit-knock-offs/count/sale-credit-master/{saleCreditMasterRefId}
     */
    @GetMapping("/count/sale-credit-master/{saleCreditMasterRefId}")
    @PermitAll
    public ResponseEntity<Long> countBySaleCreditMasterRefId(@PathVariable Integer saleCreditMasterRefId) {
        logger.info("Counting SaleCreditKnockOff records for Sale Credit Master: {}", saleCreditMasterRefId);
        long count = saleCreditKnockOffService.countBySaleCreditMasterRefId(saleCreditMasterRefId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get knock-off records by company and Sale Credit Master
     * GET /api/sale-credit-knock-offs/company/{companyRefId}/sale-credit-master/{saleCreditMasterRefId}
     */
    @GetMapping("/company/{companyRefId}/sale-credit-master/{saleCreditMasterRefId}")
    @PermitAll
    public ResponseEntity<List<SaleCreditKnockOffDto>> getByCompanyAndSaleCreditMaster(
            @PathVariable Integer companyRefId, @PathVariable Integer saleCreditMasterRefId) {
        logger.info("Fetching SaleCreditKnockOff records for company: {} and Sale Credit Master: {}", companyRefId, saleCreditMasterRefId);
        List<SaleCreditKnockOffDto> records = saleCreditKnockOffService.getByCompanyAndSaleCreditMaster(companyRefId, saleCreditMasterRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Delete all knock-off records for a Sale Credit Master
     * DELETE /api/sale-credit-knock-offs/sale-credit-master/{saleCreditMasterRefId}
     */
    @DeleteMapping("/sale-credit-master/{saleCreditMasterRefId}")
    @PermitAll
    public ResponseEntity<?> deleteAllBySaleCreditMasterRefId(@PathVariable Integer saleCreditMasterRefId) {
        logger.info("Deleting all SaleCreditKnockOff records for Sale Credit Master: {}", saleCreditMasterRefId);
        try {
            saleCreditKnockOffService.deleteAllBySaleCreditMasterRefId(saleCreditMasterRefId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            logger.error("Error deleting SaleCreditKnockOff records", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting SaleCreditKnockOff records: " + e.getMessage());
        }
    }
}


