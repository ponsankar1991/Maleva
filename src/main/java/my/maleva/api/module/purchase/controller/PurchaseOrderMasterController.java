package my.maleva.api.module.purchase.controller;

import my.maleva.api.module.purchase.dto.PurchaseOrderMasterDto;
import my.maleva.api.module.purchase.service.PurchaseOrderMasterService;
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
import my.maleva.api.module.purchase.dto.EditPurchaseOrderMasterRequestDto;
import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;

/**
 * PurchaseOrderMaster REST Controller
 * Handles all RESTful API endpoints for PurchaseOrderMaster operations
 * Base URL: /api/purchase-orders
 */
@RestController
@RequestMapping("/api/purchase-orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PurchaseOrderMasterController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderMasterController.class);

    @Autowired
    private PurchaseOrderMasterService purchaseOrderMasterService;

    /**
     * Get all PurchaseOrderMaster records by company ID
     * GET /api/purchase-orders/company/{companyRefId}
     */
    @GetMapping("/company/{companyRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseOrderMasterDto>> getAllByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching all PurchaseOrderMaster records for company: {}", companyRefId);
        List<PurchaseOrderMasterDto> records = purchaseOrderMasterService.getAllByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get active PurchaseOrderMaster records by company ID
     * GET /api/purchase-orders/company/{companyRefId}/active
     */
    @GetMapping("/company/{companyRefId}/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseOrderMasterDto>> getActiveByCompanyId(
            @PathVariable Integer companyRefId) {
        logger.info("Fetching active PurchaseOrderMaster records for company: {}", companyRefId);
        List<PurchaseOrderMasterDto> records = purchaseOrderMasterService.getActiveByCompanyId(companyRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseOrderMaster by ID
     * GET /api/purchase-orders/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching PurchaseOrderMaster by ID: {}", id);
        Optional<PurchaseOrderMasterDto> record = purchaseOrderMasterService.getById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseOrderMaster not found with ID: " + id);
        }
    }

    /**
     * Create new PurchaseOrderMaster record
     * POST /api/purchase-orders
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody PurchaseOrderMasterDto dto) {
        logger.info("Creating new PurchaseOrderMaster for company: {}", dto.getCompanyRefId());

        try {
            PurchaseOrderMasterDto created = purchaseOrderMasterService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating PurchaseOrderMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating PurchaseOrderMaster: " + e.getMessage());
        }
    }

    /**
     * Update PurchaseOrderMaster record
     * PUT /api/purchase-orders/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody PurchaseOrderMasterDto dto) {
        logger.info("Updating PurchaseOrderMaster with ID: {}", id);

        try {
            PurchaseOrderMasterDto updated = purchaseOrderMasterService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("PurchaseOrderMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseOrderMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating PurchaseOrderMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating PurchaseOrderMaster: " + e.getMessage());
        }
    }

    /**
     * Delete PurchaseOrderMaster record
     * DELETE /api/purchase-orders/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting PurchaseOrderMaster with ID: {}", id);

        try {
            boolean deleted = purchaseOrderMasterService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("PurchaseOrderMaster not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting PurchaseOrderMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting PurchaseOrderMaster: " + e.getMessage());
        }
    }

    /**
     * Get PurchaseOrderMaster by invoice number
     * GET /api/purchase-orders/company/{companyRefId}/invoice/{invoiceNo}
     */
    @GetMapping("/company/{companyRefId}/invoice/{invoiceNo}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getByInvoiceNo(
            @PathVariable Integer companyRefId,
            @PathVariable String invoiceNo) {
        logger.info("Fetching PurchaseOrderMaster by invoice number: {} for company: {}", invoiceNo, companyRefId);
        if (companyRefId == null || invoiceNo == null || invoiceNo.trim().isEmpty()) {
            logger.warn("Invalid request: companyRefId or invoiceNo is null/empty");
            return ResponseEntity.badRequest().body("companyRefId and invoiceNo must be provided");
        }
        try {
            Optional<PurchaseOrderMasterDto> record = purchaseOrderMasterService.getByInvoiceNo(companyRefId, invoiceNo);
            if (record.isPresent()) {
                return ResponseEntity.ok(record.get());
            } else {
                logger.warn("PurchaseOrderMaster not found with invoice: {} for company: {}", invoiceNo, companyRefId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(String.format("PurchaseOrderMaster not found with invoice: %s for company: %d", invoiceNo, companyRefId));
            }
        } catch (Exception e) {
            logger.error("Error fetching PurchaseOrderMaster by invoice: {} for company: {}", invoiceNo, companyRefId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching PurchaseOrderMaster: " + e.getMessage());
        }
    }

    /**
     * Get PurchaseOrderMaster by supplier
     * GET /api/purchase-orders/company/{companyRefId}/supplier/{supplierRefId}
     */
    @GetMapping("/company/{companyRefId}/supplier/{supplierRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseOrderMasterDto>> getBySupplier(
            @PathVariable Integer companyRefId,
            @PathVariable Integer supplierRefId) {
        logger.info("Fetching PurchaseOrderMaster for supplier: {}", supplierRefId);
        List<PurchaseOrderMasterDto> records = purchaseOrderMasterService.getBySupplier(companyRefId, supplierRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseOrderMaster by sale type
     * GET /api/purchase-orders/company/{companyRefId}/sale-type/{saleType}
     */
    @GetMapping("/company/{companyRefId}/sale-type/{saleType}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseOrderMasterDto>> getBySaleType(
            @PathVariable Integer companyRefId,
            @PathVariable String saleType) {
        logger.info("Fetching PurchaseOrderMaster by sale type: {}", saleType);
        List<PurchaseOrderMasterDto> records = purchaseOrderMasterService.getBySaleType(companyRefId, saleType);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseOrderMaster by date range
     * GET /api/purchase-orders/company/{companyRefId}/date-range?startDate=2026-01-01&endDate=2026-02-28
     */
    @GetMapping("/company/{companyRefId}/date-range")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseOrderMasterDto>> getByDateRange(
            @PathVariable Integer companyRefId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        logger.info("Fetching PurchaseOrderMaster between dates: {} to {}", startDate, endDate);
        List<PurchaseOrderMasterDto> records = purchaseOrderMasterService.getByDateRange(companyRefId, startDate, endDate);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseOrderMaster by employee
     * GET /api/purchase-orders/company/{companyRefId}/employee/{employeeRefId}
     */
    @GetMapping("/company/{companyRefId}/employee/{employeeRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<List<PurchaseOrderMasterDto>> getByEmployee(
            @PathVariable Integer companyRefId,
            @PathVariable Integer employeeRefId) {
        logger.info("Fetching PurchaseOrderMaster for employee: {}", employeeRefId);
        List<PurchaseOrderMasterDto> records = purchaseOrderMasterService.getByEmployee(companyRefId, employeeRefId);
        return ResponseEntity.ok(records);
    }

    /**
     * Get PurchaseOrderMaster by CNumber
     * GET /api/purchase-orders/company/{companyRefId}/cnumber/{cNumber}
     */
    @GetMapping("/company/{companyRefId}/cnumber/{cNumber}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> getByCNumber(
            @PathVariable Integer companyRefId,
            @PathVariable Integer cNumber) {
        logger.info("Fetching PurchaseOrderMaster by CNumber: {} for company: {}", cNumber, companyRefId);
        if (companyRefId == null || cNumber == null) {
            logger.warn("Invalid request: companyRefId or cNumber is null");
            return ResponseEntity.badRequest().body("companyRefId and cNumber must be provided");
        }
        try {
            Optional<PurchaseOrderMasterDto> record = purchaseOrderMasterService.getByCNumber(companyRefId, cNumber);
            if (record.isPresent()) {
                return ResponseEntity.ok(record.get());
            } else {
                logger.warn("PurchaseOrderMaster not found with CNumber: {} for company: {}", cNumber, companyRefId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(String.format("PurchaseOrderMaster not found with CNumber: %d for company: %d", cNumber, companyRefId));
            }
        } catch (Exception e) {
            logger.error("Error fetching PurchaseOrderMaster by CNumber: {} for company: {}", cNumber, companyRefId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching PurchaseOrderMaster: " + e.getMessage());
        }
    }

    /**
     * Count PurchaseOrderMaster records by company
     * GET /api/purchase-orders/company/{companyRefId}/count
     */
    @GetMapping("/company/{companyRefId}/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> countByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting PurchaseOrderMaster records for company: {}", companyRefId);
        long count = purchaseOrderMasterService.countByCompanyId(companyRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    /**
     * Count active PurchaseOrderMaster records by company
     * GET /api/purchase-orders/company/{companyRefId}/count/active
     */
    @GetMapping("/company/{companyRefId}/count/active")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> countActiveByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Counting active PurchaseOrderMaster records for company: {}", companyRefId);
        long count = purchaseOrderMasterService.countActiveByCompanyId(companyRefId);
        return ResponseEntity.ok("Active Total: " + count);
    }

    /**
     * Activate PurchaseOrderMaster record
     * POST /api/purchase-orders/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        logger.info("Activating PurchaseOrderMaster with ID: {}", id);

        try {
            PurchaseOrderMasterDto activated = purchaseOrderMasterService.activate(id);
            return ResponseEntity.ok(activated);
        } catch (RuntimeException e) {
            logger.error("PurchaseOrderMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseOrderMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error activating PurchaseOrderMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error activating PurchaseOrderMaster: " + e.getMessage());
        }
    }

    /**
     * Deactivate PurchaseOrderMaster record
     * POST /api/purchase-orders/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        logger.info("Deactivating PurchaseOrderMaster with ID: {}", id);

        try {
            PurchaseOrderMasterDto deactivated = purchaseOrderMasterService.deactivate(id);
            return ResponseEntity.ok(deactivated);
        } catch (RuntimeException e) {
            logger.error("PurchaseOrderMaster not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseOrderMaster not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error deactivating PurchaseOrderMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deactivating PurchaseOrderMaster: " + e.getMessage());
        }
    }

    /**
     * Edit/Fetch PurchaseOrderMaster with all details
     * POST /api/purchase-orders/edit
     *
     * Fetches a PurchaseOrderMaster record along with all associated details.
     * Can lookup by ID or by CNumber (PurchaseOrderMasterNo).
     * Only returns records with pStatus = 0 (unprocessed).
     */
    @PostMapping("/edit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<?> editPurchaseOrderMaster(@Valid @RequestBody EditPurchaseOrderMasterRequestDto request) {
        logger.info("EditPurchaseOrderMaster request - Company: {}, ID: {}, CNumber: {}",
                request.getCompanyId(), request.getId(), request.getPurchaseOrderMasterNo());

        try {
            PurchaseOrderMasterDto result = purchaseOrderMasterService.editPurchaseOrderMaster(request);
            logger.info("Successfully fetched PurchaseOrderMaster for editing");
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            logger.warn("PurchaseOrderMaster not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("PurchaseOrderMaster not found: " + e.getMessage());
        } catch (InvalidRequestException e) {
            logger.warn("Invalid request for EditPurchaseOrderMaster: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching PurchaseOrderMaster for editing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching PurchaseOrderMaster: " + e.getMessage());
        }
    }
}
