package my.maleva.api.module.supplier.controller;

import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.supplier.dto.SupplierDto;
import my.maleva.api.module.supplier.dto.SupplierGridPage;
import my.maleva.api.module.supplier.dto.SupplierGridRequest;
import my.maleva.api.module.supplier.dto.SupplierLookupOption;
import my.maleva.api.module.supplier.dto.SupplierQneSyncResult;
import my.maleva.api.module.supplier.service.SupplierQneSyncService;
import my.maleva.api.module.supplier.dto.SupplierQneOutcome;
import my.maleva.api.module.supplier.dto.SupplierSaveResponse;
import my.maleva.api.module.supplier.dto.SupplierSearchResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.supplier.dto.SupplierExtendedResponse;
import my.maleva.api.module.supplier.service.SupplierQneService;
import my.maleva.api.module.supplier.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

import my.maleva.api.common.dto.ResponseViewModel;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * SupplierController - REST Controller for Supplier API
 */
@RestController
@RequestMapping("/api/suppliers")
@PermitAll
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    @Autowired
    private SupplierService service;

    @Autowired
    private SupplierQneService qneService;

    @Autowired
    private SupplierQneSyncService syncService;

    /**
     * Repair suppliers whose QNE code is set but whose QNE id was never
     * stored — the Java port of legacy UpdateSupplierId1.
     * POST /api/suppliers/qne/backfill?companyId=1
     */
    @PostMapping("/qne/backfill")
    public ResponseEntity<?> qneBackfill(@RequestParam Integer companyId) {
        return QnePushResponses.toResponse(qneService.backfill(companyId));
    }

    /* ================= SupplierView grid ================= */

    /**
     * The SupplierView grid's search — legacy {@code SelectSupplier} and the
     * grid's filter row, run on the server against every supplier.
     * GET /api/suppliers/search?companyId=6&type=VENDOR&keyword=petron&city=klang&sortBy=supplierName&sortDir=asc&page=0&size=50
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SupplierGridPage>> search(@ModelAttribute SupplierGridRequest request) {
        SupplierGridPage page = service.search(request);
        return ResponseEntity.ok(ApiResponse.success(page.total() + " supplier(s)", page));
    }

    /**
     * "Update from QNE" — legacy {@code UpdateSupplierId}: pull QNE's supplier
     * list, repair stored QNE ids, create the suppliers missing here. Always
     * 200 with the outcome in the body; a QNE refusal is a status, not a crash.
     * POST /api/suppliers/qne/sync?companyId=6
     */
    @PostMapping("/qne/sync")
    public ResponseEntity<ApiResponse<SupplierQneSyncResult>> syncFromQne(@RequestParam Integer companyId) {
        SupplierQneSyncResult result = syncService.syncFromQne(companyId);
        return ResponseEntity.ok(ApiResponse.success(result.message(), result));
    }

    /** MSIC code combo — legacy {@code GetMSICCode}. GET /api/suppliers/msic-codes */
    @GetMapping("/msic-codes")
    public ResponseEntity<ApiResponse<List<SupplierLookupOption>>> msicCodes() {
        List<SupplierLookupOption> options = service.msicCodes();
        return ResponseEntity.ok(ApiResponse.success(options.size() + " MSIC code(s)", options));
    }

    /** Self Billed Type combo — legacy {@code GetSelfbilled}. GET /api/suppliers/self-billed-types */
    @GetMapping("/self-billed-types")
    public ResponseEntity<ApiResponse<List<SupplierLookupOption>>> selfBilledTypes() {
        List<SupplierLookupOption> options = service.selfBilledTypes();
        return ResponseEntity.ok(ApiResponse.success(options.size() + " self-billed type(s)", options));
    }

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
     * Legacy {@code InsertSupplier} for a new supplier, in its order: the save
     * (SP_Supplier's statements, committed when {@code service.create}
     * returns), THEN the QNE push, then the answer.
     *
     * <p>The answer carries the stored row and the QNE outcome side by side.
     * Legacy answered a QNE refusal with {@code ok = false} for a supplier it
     * had already committed; the screen kept EditId at 0 and a second Save made
     * a second supplier. Here the save is reported as done, QNE's message is
     * shown, and the screen switches to the saved supplier so UPDATE retries
     * the push.
     *
     * <p>Validation failures ({@code InvalidRequestException}) go to the global
     * handler: 400 with the message, nothing saved.
     * POST /api/suppliers
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SupplierSaveResponse>> create(@Valid @RequestBody SupplierDto dto) {
        logger.info("Creating new Supplier");
        SupplierDto created = service.create(dto);
        SupplierSaveResponse body = pushToQne(created, dto);
        return ResponseEntity
                .created(URI.create("/api/suppliers/" + created.getId()))
                .body(ApiResponse.success("Supplier created successfully", body));
    }

    /**
     * Legacy {@code InsertSupplier} for an existing supplier. The push still
     * runs: legacy's condition was {@code Id == 0 || QNECode blank}, so an edit
     * of a supplier QNE never received is created there now. One QNE already
     * has is left alone — the update payload legacy built was never sent.
     * PUT /api/suppliers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierSaveResponse>> update(@PathVariable Integer id,
                                                                    @Valid @RequestBody SupplierDto dto) {
        logger.info("Updating Supplier with ID: {}", id);
        SupplierDto updated = service.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", pushToQne(updated, dto)));
    }

    /**
     * Push with the values as typed, handing over the row the save already
     * loaded. When the push writes QNEId/QNECode back, the same two values are
     * set on that row here instead of reading it from the database a third time.
     */
    private SupplierSaveResponse pushToQne(SupplierDto saved, SupplierDto typed) {
        SupplierQneOutcome qne = qneService.pushSaved(saved, typed);
        if (qne.status() == SupplierQneOutcome.Status.PUSHED) {
            saved.setQneId(qne.qneId());
            saved.setQneCode(qne.qneCode());
        }
        return new SupplierSaveResponse(saved, qne);
    }

    /**
     * The screen's DELETE — legacy {@code DeleteSupplier}, which sets Active = 2.
     * PUT /api/suppliers/{id}/soft-delete?companyId=6
     */
    @PutMapping("/{id}/soft-delete")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable Integer id,
                                                        @RequestParam Integer companyId) {
        service.softDelete(id, companyId);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted", null));
    }

    /**
     * Delete Supplier (hard delete — the screens use soft-delete)
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
        SupplierDto result = service.processSupplierBatch(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pushToQne(result, dto));
    }

    /**
     * Select Supplier with pagination and search filters
     * POST /api/suppliers/select
     *
     * Query Parameters:
     * - comid: Company ID
     * - startindex: Starting index for pagination (use -1 for last page)
     * - pageCount: Records per page
     * - keyword: Search keyword (empty for all records)
     * - column: Search column (SupplierName, MobileNo, Id, All)
     * - type: Supplier type filter (empty/ALL/null for no filter)
     */
    @PostMapping("/select")
    public ResponseEntity<?> selectSupplier(
            @RequestParam(value = "comid", required = false) Integer comid,
            @RequestParam(value = "startindex", required = false) Integer startindex,
            @RequestParam(value = "pageCount", required = false) Integer pageCount,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "column", required = false) String column,
            @RequestParam(value = "type", required = false) String type) {
        logger.info("Select Supplier - comid: {}, startindex: {}, pageCount: {}, keyword: {}, column: {}, type: {}",
                   comid, startindex, pageCount, keyword, column, type);
        try {
            SupplierSearchResponse response = service.selectSupplier(comid, startindex, pageCount, keyword, column, type);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error in selectSupplier", ex);
            return ResponseEntity.ok(SupplierSearchResponse.builder()
                    .ok(false)
                    .message("Error: " + ex.getMessage())
                    .build());
        }
    }

    /**
     * Get Supplier combo list for dropdowns/comboboxes
     * Equivalent to .NET GetSupplier(int Comid, string type) method
     *
     * GET /api/suppliers/combo?comid=1
     * GET /api/suppliers/combo?comid=1&type=LOCAL
     *
     * @param comid Company ID (required)
     * @param type Supplier Type filter (optional - null/""/ALL for no type filter)
     * @return ResponseEntity with ResponseViewModel containing List<SupplierComboList>
     */
    @GetMapping("/combo")
    public ResponseEntity<?> getSupplier(
            @RequestParam(value = "comid", required = false) Integer comid,
            @RequestParam(value = "type", required = false) String type) {
        logger.info("Get Supplier combo list - comid: {}, type: {}", comid, type);

        try {
            if (comid == null || comid <= 0) {
                logger.warn("Invalid request: comid is missing or invalid");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseViewModel.error("Company ID is required and must be greater than 0", 400));
            }

            ResponseViewModel response = service.getSupplier(comid, type);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                int statusCode = response.getStatusCode() != null ? response.getStatusCode() : 400;
                return ResponseEntity.status(statusCode).body(response);
            }

        } catch (Exception ex) {
            logger.error("Error in getSupplier endpoint", ex);
            ResponseViewModel errorResponse = ResponseViewModel.error(
                    "Internal server error: " + ex.getMessage(),
                    500
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Select All Suppliers with joined master data
     * Equivalent to .NET SelectSupplierAll(int Comid) method
     *
     * GET /api/suppliers/select-all?comid=1
     *
     * @param comid Company Reference ID (required)
     * @return ResponseEntity with List of SupplierExtendedResponse
     */
    @GetMapping("/select-all")
    public ResponseEntity<?> selectSupplierAll(
            @RequestParam(value = "comid", required = false) Integer comid) {
        logger.info("Select All Suppliers - comid: {}", comid);

        try {
            if (comid == null || comid <= 0) {
                logger.warn("Invalid request: comid is missing or invalid");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseViewModel.error("Company ID is required and must be greater than 0", 400));
            }

            List<SupplierExtendedResponse> suppliers = service.selectSupplierAll(comid);

            logger.info("Successfully fetched {} suppliers for company: {}", suppliers.size(), comid);

            return ResponseEntity.ok(
                ResponseViewModel.success(
                    suppliers,
                    "Successfully retrieved " + suppliers.size() + " supplier records",
                    200
                )
            );

        } catch (Exception ex) {
            logger.error("Error in selectSupplierAll endpoint", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseViewModel.error(
                        "Internal server error: " + ex.getMessage(),
                        500
                    ));
        }
    }

}
