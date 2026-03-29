package my.maleva.api.module.purchase.controller;

import my.maleva.api.module.purchase.dto.PurchaseMasterDto;
import my.maleva.api.module.purchase.service.PurchaseMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * PurchaseMaster REST Controller
 * Handles all RESTful API endpoints for PurchaseMaster operations
 * Base URL: /api/purchase-masters
 */
@RestController
@RequestMapping("/api/purchase-masters")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PurchaseMasterController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseMasterController.class);

    @Autowired
    private PurchaseMasterService purchaseMasterService;

    /**
     * Get all PurchaseMaster records by company ID
     * GET /api/purchase-masters/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseMasterDto>> getAllByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching all PurchaseMaster records for company: {}", companyRefId);
        List<PurchaseMasterDto> records = purchaseMasterService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get active PurchaseMaster records by company ID
     * GET /api/purchase-masters/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseMasterDto>> getActiveByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching active PurchaseMaster records for company: {}", companyRefId);
        List<PurchaseMasterDto> records = purchaseMasterService.getActiveByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseMaster by ID
     * GET /api/purchase-masters/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching PurchaseMaster by ID: {}", id);
        Optional<PurchaseMasterDto> record = purchaseMasterService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseMaster not found with ID: " + id);
        }
    }

    /**
     * Create new PurchaseMaster record
     * POST /api/purchase-masters
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody PurchaseMasterDto dto) {
        logger.info("Creating new PurchaseMaster for company: {}", dto.getCompanyRefId());

        try {
            PurchaseMasterDto created = purchaseMasterService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating PurchaseMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating PurchaseMaster: " + e.getMessage());
        }
    }

    /**
     * Update PurchaseMaster record
     * PUT /api/purchase-masters/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody PurchaseMasterDto dto) {
        logger.info("Updating PurchaseMaster with ID: {}", id);

        try {
            PurchaseMasterDto updated = purchaseMasterService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("PurchaseMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating PurchaseMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating PurchaseMaster: " + e.getMessage());
        }
    }

    /**
     * Delete PurchaseMaster record
     * DELETE /api/purchase-masters/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting PurchaseMaster with ID: {}", id);

        try {
            boolean deleted = purchaseMasterService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("PurchaseMaster not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting PurchaseMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting PurchaseMaster: " + e.getMessage());
        }
    }

    /**
     * Get PurchaseMaster by invoice number
     * GET /api/purchase-masters/company/{companyRefId}/invoice/{invoiceNo}
     */
    @GetMapping("/company/{companyRefId}/invoice/{invoiceNo}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getByInvoiceNo(
            @PathVariable Integer companyRefId,
            @PathVariable String invoiceNo) {
        logger.info("Fetching PurchaseMaster by invoice number: {} for company: {}", invoiceNo, companyRefId);
        if (companyRefId == null || invoiceNo == null || invoiceNo.trim().isEmpty()) {
            logger.warn("Invalid request: companyRefId or invoiceNo is null/empty");
            return ResponseEntity.badRequest().body("companyRefId and invoiceNo must be provided");
        }
        try {
            Optional<PurchaseMasterDto> record = purchaseMasterService.getByInvoiceNo(companyRefId, invoiceNo);
            if (record.isPresent()) {
                return ResponseEntity.ok(record.get());
            } else {
                logger.warn("PurchaseMaster not found with invoice: {} for company: {}", invoiceNo, companyRefId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(String.format("PurchaseMaster not found with invoice: %s for company: %d", invoiceNo, companyRefId));
            }
        } catch (Exception e) {
            logger.error("Error fetching PurchaseMaster by invoice: {} for company: {}", invoiceNo, companyRefId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching PurchaseMaster: " + e.getMessage());
        }
    }

    /**
     * Get PurchaseMaster by supplier
     * GET /api/purchase-masters/company/{companyRefId}/supplier/{supplierRefId}
     */
    @GetMapping("/company/{companyRefId}/supplier/{supplierRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseMasterDto>> getBySupplier(
            @PathVariable Integer companyRefId,
            @PathVariable Integer supplierRefId) {
        logger.info("Fetching PurchaseMaster for supplier: {}", supplierRefId);
        List<PurchaseMasterDto> records = purchaseMasterService.getBySupplier(companyRefId, supplierRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseMaster by sale type
     * GET /api/purchase-masters/company/{companyRefId}/sale-type/{saleType}
     */
    @GetMapping("/company/{companyRefId}/sale-type/{saleType}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseMasterDto>> getBySaleType(
            @PathVariable Integer companyRefId,
            @PathVariable String saleType) {
        logger.info("Fetching PurchaseMaster by sale type: {}", saleType);
        List<PurchaseMasterDto> records = purchaseMasterService.getBySaleType(companyRefId, saleType);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseMaster by date range
     * GET /api/purchase-masters/company/{companyRefId}/date-range?startDate=2026-01-01&endDate=2026-02-28
     */
    @GetMapping("/company/{companyRefId}/date-range")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseMasterDto>> getByDateRange(
            @PathVariable Integer companyRefId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        logger.info("Fetching PurchaseMaster between dates: {} to {}", startDate, endDate);
        List<PurchaseMasterDto> records = purchaseMasterService.getByDateRange(companyRefId, startDate, endDate);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseMaster by employee
     * GET /api/purchase-masters/company/{companyRefId}/employee/{employeeRefId}
     */
    @GetMapping("/company/{companyRefId}/employee/{employeeRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseMasterDto>> getByEmployee(
            @PathVariable Integer companyRefId,
            @PathVariable Integer employeeRefId) {
        logger.info("Fetching PurchaseMaster for employee: {}", employeeRefId);
        List<PurchaseMasterDto> records = purchaseMasterService.getByEmployee(companyRefId, employeeRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseMaster by CNumber
     * GET /api/purchase-masters/company/{companyRefId}/cnumber/{cNumber}
     */
    @GetMapping("/company/{companyRefId}/cnumber/{cNumber}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getByCNumber(
            @PathVariable Integer companyRefId,
            @PathVariable Integer cNumber) {
        logger.info("Fetching PurchaseMaster by CNumber: {} for company: {}", cNumber, companyRefId);
        if (companyRefId == null || cNumber == null) {
            logger.warn("Invalid request: companyRefId or cNumber is null");
            return ResponseEntity.badRequest().body("companyRefId and cNumber must be provided");
        }
        try {
            Optional<PurchaseMasterDto> record = purchaseMasterService.getByCNumber(companyRefId, cNumber);
            if (record.isPresent()) {
                return ResponseEntity.ok(record.get());
            } else {
                logger.warn("PurchaseMaster not found with CNumber: {} for company: {}", cNumber, companyRefId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(String.format("PurchaseMaster not found with CNumber: %d for company: %d", cNumber, companyRefId));
            }
        } catch (Exception e) {
            logger.error("Error fetching PurchaseMaster by CNumber: {} for company: {}", cNumber, companyRefId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching PurchaseMaster: " + e.getMessage());
        }
    }

    /**
     * Count PurchaseMaster records by company
     * GET /api/purchase-masters/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> countByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting PurchaseMaster records for company: {}", companyRefId);
        long count = purchaseMasterService.countByCompanyId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active PurchaseMaster records by company
     * GET /api/purchase-masters/company/{companyRefId}/count/active
     */
    @GetMapping("/company/{companyRefId}/count/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> countActiveByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting active PurchaseMaster records for company: {}", companyRefId);
        long count = purchaseMasterService.countActiveByCompanyId(companyRefId);
        return ResponseEntity.ok("Active Total: " + count);
    }

    /**
     * Activate PurchaseMaster record
     * POST /api/purchase-masters/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating PurchaseMaster with ID: {}", id);

        try {
            PurchaseMasterDto activated = purchaseMasterService.activate(id);
            return ResponseEntity.ok(activated);
        } catch (RuntimeException e) {
            logger.error("PurchaseMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error activating PurchaseMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating PurchaseMaster: " + e.getMessage());
        }
    }

    /**
     * Deactivate PurchaseMaster record
     * POST /api/purchase-masters/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating PurchaseMaster with ID: {}", id);

        try {
            PurchaseMasterDto deactivated = purchaseMasterService.deactivate(id);
            return ResponseEntity.ok(deactivated);
        } catch (RuntimeException e) {
            logger.error("PurchaseMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error deactivating PurchaseMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deactivating PurchaseMaster: " + e.getMessage());
        }
    }
}

