package my.maleva.api.module.purchase.controller;

import my.maleva.api.module.purchase.dto.PurchaseMasterDto;
import my.maleva.api.module.purchase.service.PurchaseMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import my.maleva.api.module.purchase.dto.*;
import jakarta.validation.constraints.Positive;

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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
     * Delete PurchaseMaster record (Soft Delete - set Active=2)
     * Equivalent to .NET DeletePurchaseMaster method
     * DELETE /api/purchase-masters/delete/{id}?companyId={companyId}
     */
    @DeleteMapping("/delete/{id}")
    @PermitAll
    public ResponseEntity<DeletePurchaseMasterResponseDto> deletePurchaseMaster(
            @PathVariable @Positive Integer id,
            @RequestParam(name = "companyId") @Positive Integer companyId) {
        logger.info("DeletePurchaseMaster request received - id: {}, companyId: {}", id, companyId);

        try {
            boolean deleted = purchaseMasterService.softDelete(id, companyId);
            if (deleted) {
                return ResponseEntity.ok(DeletePurchaseMasterResponseDto.builder()
                        .ok(true)
                        .message("PurchaseMaster DeleteSuccess")
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(DeletePurchaseMasterResponseDto.builder()
                                .ok(false)
                                .message("PurchaseMaster not found")
                                .build());
            }
        } catch (Exception e) {
            logger.error("Error in DeletePurchaseMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DeletePurchaseMasterResponseDto.builder()
                            .ok(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Get PurchaseMaster by invoice number
     * GET /api/purchase-masters/company/{companyRefId}/invoice/{invoiceNo}
     */
    @GetMapping("/company/{companyRefId}/invoice/{invoiceNo}")
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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

    /**
     * Check Edit Amount - Calculate total payment amount for a purchase order
     * POST /api/purchase-masters/check-edit-amount
     */
    @PostMapping("/check-edit-amount")
    @PermitAll
    public ResponseEntity<?> checkEditAmount(@Valid @RequestBody CheckEditAmountRequestDto request) {
        logger.info("CheckEditAmount request for purchase ID: {} in company: {}", request.getPurchaseId(), request.getCompanyId());

        try {
            BigDecimal totalAmount = purchaseMasterService.checkEditAmount(request.getCompanyId(), request.getPurchaseId());
            CheckEditAmountResponseDto response = CheckEditAmountResponseDto.builder()
                    .totalPaymentAmount(totalAmount)
                    .message("Total payment amount calculated successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error calculating payment amount", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error calculating payment amount: " + e.getMessage());
        }
    }

    /**
     * Get max PurchaseMaster No
     * Equivalent to .NET MaxPurchaseMasterNo method
     * GET /api/purchase-masters/max-purchase-master-no?companyId={companyId}
     */
    @GetMapping("/max-purchase-master-no")
    @PermitAll
    public ResponseEntity<MaxPurchaseMasterNoResponseDto> getMaxPurchaseMasterNo(
            @RequestParam @Positive Integer companyId) {
        logger.info("MaxPurchaseMasterNo request for company: {}", companyId);

        try {
            String maxNo = purchaseMasterService.getMaxPurchaseMasterNo(companyId);
            return ResponseEntity.ok(MaxPurchaseMasterNoResponseDto.builder()
                    .ok(true)
                    .no(maxNo)
                    .build());
        } catch (Exception e) {
            logger.error("Error in MaxPurchaseMasterNo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MaxPurchaseMasterNoResponseDto.builder()
                            .ok(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Get distinct descriptions from PurchaseMaster
     * Equivalent to .NET SelectDescription method
     * GET /api/purchase-masters/select-description?companyId={companyId}
     */
    @GetMapping("/select-description")
    @PermitAll
    public ResponseEntity<SelectDescriptionResponseDto> selectDescription(
            @RequestParam @Positive Integer companyId) {
        logger.info("SelectDescription request for company: {}", companyId);

        try {
            List<String> descriptions = purchaseMasterService.getDistinctDescriptions(companyId);
            return ResponseEntity.ok(SelectDescriptionResponseDto.builder()
                    .ok(true)
                    .data(descriptions)
                    .build());
        } catch (Exception e) {
            logger.error("Error in SelectDescription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SelectDescriptionResponseDto.builder()
                            .ok(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Insert PurchaseMaster records using stored procedure
     * Equivalent to .NET InsertPurchaseMaster method
     * POST /api/purchase-masters/insert?companyId={companyId}
     */
    @PostMapping("/insert")
    @PermitAll
    public ResponseEntity<InsertPurchaseMasterResponseDto> insertPurchaseMaster(
            @Valid @RequestBody List<PurchaseMasterDto> purchaseMasters,
            @RequestParam @Positive Integer companyId) {
        logger.info("InsertPurchaseMaster request received - companyId: {}, items: {}", companyId, purchaseMasters.size());

        try {
            InsertPurchaseMasterResponseDto response = purchaseMasterService.insertPurchaseMaster(purchaseMasters, companyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in InsertPurchaseMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(InsertPurchaseMasterResponseDto.builder()
                            .ok(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Get spare parts report view with multiple filters
     * Equivalent to .NET SelectSparePartsView method
     * Supports filtering by supplier, employee, driver, truck, product, date range, and search
     * POST /api/purchase-masters/select-spare-parts-view
     */
    @PostMapping("/select-spare-parts-view")
    @PermitAll
    public ResponseEntity<SelectSparePartsViewResponseDto> selectSparePartsView(
            @Valid @RequestBody SelectSparePartsViewRequestDto request) {
        logger.info("SelectSparePartsView request received - companyId: {}, supplier: {}, employee: {}, driver: {}, truck: {}, product: {}, search: {}",
                   request.getCompanyId(), request.getSupplierId(), request.getEmployeeId(),
                   request.getDriverId(), request.getTruckId(), request.getProductId(),
                   request.getSearch());

        try {
            SelectSparePartsViewResponseDto response = purchaseMasterService.selectSparePartsView(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in SelectSparePartsView", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SelectSparePartsViewResponseDto.builder()
                            .ok(false)
                            .message("Error retrieving spare parts report: " + e.getMessage())
                            .data(List.of())
                            .build());
        }
    }

    /**
     * Get purchase master records with multiple filters and combined view
     * Equivalent to .NET SelectPurchaseMaster method
     * Retrieves both master and detail records in a single combined response
     * 
     * Supports flexible filtering:
     * <ul>
     *   <li>Supplier filter: supplierId > 0</li>
     *   <li>Employee filter: employeeId > 0</li>
     *   <li>Driver filter: driverId > 0</li>
     *   <li>Truck filter: truckId > 0</li>
     *   <li>Product filter: productId > 0</li>
     *   <li>Search filter: By CNumberDisplay or InvoiceNo (overrides date filter)</li>
     *   <li>Date filter: By InvoiceDate (invoiceCheck=1) or SaleDate (invoiceCheck=0)</li>
     * </ul>
     * 
     * POST /api/purchase-masters/select-purchase-master
     * Request body: {
     *   "Comid": 1,
     *   "Fromdate": "2024-01-01",
     *   "Todate": "2024-12-31",
     *   "Id": 0,
     *   "Employeeid": 0,
     *   "Search": null,
     *   "invoicecheck": 0,
     *   "DriverId": 0,
     *   "TruckId": 0,
     *   "ProductId": 0
     * }
     */
    @PostMapping("/select-purchase-master")
    @PermitAll
    public ResponseEntity<SelectPurchaseMasterResponseDto> selectPurchaseMaster(
            @Valid @RequestBody SelectPurchaseMasterRequestDto request) {
        logger.info("SelectPurchaseMaster request received - companyId: {}, supplier: {}, employee: {}, driver: {}, truck: {}, product: {}, search: {}",
                   request.getCompanyId(), request.getSupplierId(), request.getEmployeeId(),
                   request.getDriverId(), request.getTruckId(), request.getProductId(),
                   request.getSearch());

        try {
            SelectPurchaseMasterResponseDto response = purchaseMasterService.selectPurchaseMaster(request);
            logger.info("SelectPurchaseMaster completed successfully - ok: {}, records: {}", 
                    response.isOk(), response.getData() != null ? response.getData().size() : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in SelectPurchaseMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SelectPurchaseMasterResponseDto.builder()
                            .ok(false)
                            .message("Error retrieving purchase master records: " + e.getMessage())
                            .data(List.of())
                            .build());
        }
    }

    /**
     * Get full purchase master record with all details for editing
     * Equivalent to .NET EditPurchaseMaster method
     *
     * Retrieves a single PurchaseMaster record with:
     * - All master fields (invoice, dates, amounts, etc.)
     * - All PurchaseDetails items with product information
     * - Product master data and UOM information
     *
     * When purchaseMasterNo is provided and id is 0/null, resolves ID via CNumber lookup
     *
     * POST /api/purchase-masters/edit-purchase-master
     * Request body: {
     *   "companyId": 1,
     *   "id": 1001,
     *   "purchaseMasterNo": null
     * }
     *
     * OR (with lookup by CNumber):
     * Request body: {
     *   "companyId": 1,
     *   "id": 0,
     *   "purchaseMasterNo": 5
     * }
     */
    @PostMapping("/edit-purchase-master")
    @PermitAll
    public ResponseEntity<EditPurchaseMasterResponseDto> editPurchaseMaster(
            @Valid @RequestBody EditPurchaseMasterRequestDto request) {
        logger.info("EditPurchaseMaster request received - companyId: {}, id: {}, purchaseMasterNo: {}",
                   request.getCompanyId(), request.getId(), request.getPurchaseMasterNo());

        try {
            EditPurchaseMasterResponseDto response = purchaseMasterService.editPurchaseMaster(request);
            logger.info("EditPurchaseMaster completed successfully - ok: {}, records: {}",
                    response.isOk(), response.getData() != null ? response.getData().size() : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in EditPurchaseMaster", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EditPurchaseMasterResponseDto.builder()
                            .ok(false)
                            .message("Error retrieving purchase master: " + e.getMessage())
                            .data(List.of())
                            .build());
        }
    }
}

