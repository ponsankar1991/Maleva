package my.maleva.api.module.billing.bill.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.billing.bill.dto.BillMasterDto;
import my.maleva.api.module.billing.bill.dto.BillMasterEditDto;
import my.maleva.api.module.billing.bill.dto.BillMasterF5ViewDto;
import my.maleva.api.module.billing.bill.dto.BillMasterInsertDto;
import my.maleva.api.module.billing.bill.dto.BillMasterSaveResponseDto;
import my.maleva.api.module.billing.bill.dto.BillMasterViewDto;
import my.maleva.api.module.billing.bill.dto.SelectBillMasterRequestDto;
import my.maleva.api.module.billing.bill.service.BillMasterService;
import my.maleva.api.module.billing.bill.service.BillMasterTransactionService;
import my.maleva.api.module.billing.bill.service.BillQneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Bill screen API — the Java replacement for the legacy
 * {@code /BillMaster/*} MVC endpoints.
 *
 * <p>Company scope arrives as {@code companyId} (query parameter) or the
 * {@code Comid} header, matching how the rest of this API is called.
 */
@RestController
@RequestMapping("/api/bills")
@Validated
@PermitAll
public class BillMasterController {

    private static final Logger logger = LoggerFactory.getLogger(BillMasterController.class);

    private final BillMasterService service;
    private final BillMasterTransactionService transactions;
    private final BillQneService qneService;

    public BillMasterController(BillMasterService service,
                                BillMasterTransactionService transactions,
                                BillQneService qneService) {
        this.service = service;
        this.transactions = transactions;
        this.qneService = qneService;
    }

    /* ── bill screen ───────────────────────────────────────────────── */

    /**
     * Next bill number, for a blank screen.
     * GET /api/bills/next-number?companyId=6
     *
     * <p>Legacy: {@code POST /BillMaster/MaxBillMasterNo}. A preview only —
     * the number is assigned when the bill is saved.
     */
    @GetMapping("/next-number")
    public ResponseEntity<ApiResponse<String>> nextNumber(@RequestParam Integer companyId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.nextBillNumber(companyId), "Next bill number generated"));
        } catch (Exception e) {
            logger.error("Error generating next bill number for company {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error generating bill number: " + e.getMessage(), 500));
        }
    }

    /**
     * Save a bill and its lines — insert when id is 0/absent, otherwise update.
     * POST /api/bills/save?companyId=6
     *
     * <p>Legacy: {@code POST /BillMaster/InsertBillMaster}, which posted an
     * array; this takes the single object it always contained.
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<BillMasterSaveResponseDto>> save(
            @Valid @RequestBody BillMasterInsertDto dto,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        Integer company = companyId != null ? companyId : comid;
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Company ID is required", 400));
        }
        try {
            BillMasterSaveResponseDto result = transactions.save(dto, company);
            if (!result.isSuccess()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(result.getMessage(), 400));
            }
            return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving bill for company {}", company, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error saving bill: " + e.getMessage(), 500));
        }
    }

    /**
     * Load one bill for editing, by id or by its running number.
     * GET /api/bills/edit?companyId=6&id=12
     * GET /api/bills/edit?companyId=6&billNumber=45
     *
     * <p>Legacy: {@code POST /BillMaster/EditBillMaster}.
     */
    @GetMapping("/edit")
    public ResponseEntity<ApiResponse<BillMasterEditDto>> edit(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer billNumber) {
        if ((id == null || id == 0) && (billNumber == null || billNumber == 0)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Provide either id or billNumber", 400));
        }
        try {
            Optional<BillMasterEditDto> bill = transactions.edit(id, billNumber, companyId);
            return bill
                    .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Bill loaded")))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Invalid bill number", 404)));
        } catch (Exception e) {
            logger.error("Error loading bill id={} number={}", id, billNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error loading bill: " + e.getMessage(), 500));
        }
    }

    /**
     * The F5 search grid: bills plus their lines.
     * POST /api/bills/search?companyId=6
     *
     * <p>Legacy: {@code POST /BillMaster/SelectBillMaster}. Dates are
     * {@code yyyy-MM-dd} (or {@code dd/MM/yyyy}); a non-empty {@code search}
     * matches the bill or supplier invoice number and ignores the dates.
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<BillMasterF5ViewDto>> search(
            @RequestBody SelectBillMasterRequestDto request,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        Integer company = firstNonNull(companyId, comid, request.getComid());
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Company ID is required", 400));
        }
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.search(request, company), "Success"));
        } catch (Exception e) {
            logger.error("Error searching bills for company {}", company, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error searching bills: " + e.getMessage(), 500));
        }
    }

    /**
     * Unpaid bills, either falling due in a date range or already overdue.
     * GET /api/bills/due?companyId=6&fromDate=2026-08-01&toDate=2026-08-31&dueInRange=true
     *
     * <p>Legacy: {@code POST /BillMaster/SelectDueBills}.
     */
    @GetMapping("/due")
    public ResponseEntity<ApiResponse<List<BillMasterViewDto>>> dueBills(
            @RequestParam Integer companyId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(defaultValue = "false") boolean dueInRange) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.dueBills(companyId, fromDate, toDate,
                            supplierId, employeeId, dueInRange),
                    "Success"));
        } catch (Exception e) {
            logger.error("Error fetching due bills for company {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching due bills: " + e.getMessage(), 500));
        }
    }

    /**
     * Soft-delete a bill (Active=2) — payment history still references it.
     * DELETE /api/bills/{id}?companyId=6
     *
     * <p>Legacy: {@code POST /BillMaster/DeleteBillMaster}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBill(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        try {
            if (!transactions.delete(id, companyId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Bill not found: " + id, 404));
            }
            return ResponseEntity.ok(ApiResponse.success(null, "Bill deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting bill {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting bill: " + e.getMessage(), 500));
        }
    }

    /* ── screen lookups ────────────────────────────────────────────── */

    /**
     * Descriptions used on earlier bills, for the description dropdown.
     * GET /api/bills/descriptions?companyId=6
     *
     * <p>Legacy: {@code POST /BillMaster/SelectDescription}.
     */
    @GetMapping("/descriptions")
    public ResponseEntity<ApiResponse<List<String>>> descriptions(@RequestParam Integer companyId) {
        return ResponseEntity.ok(ApiResponse.success(
                transactions.descriptions(companyId), "Success"));
    }

    /**
     * The supplier's currency rate, which seeds the bill's conversion.
     * GET /api/bills/supplier-currency?companyId=6&supplierId=12
     *
     * <p>Legacy: {@code POST /BillMaster/GetSupplierCurrencyValue}.
     */
    @GetMapping("/supplier-currency")
    public ResponseEntity<ApiResponse<Float>> supplierCurrency(
            @RequestParam Integer companyId,
            @RequestParam Integer supplierId) {
        Float rate = transactions.supplierCurrencyValue(companyId, supplierId);
        if (rate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No currency mapped for supplier " + supplierId, 404));
        }
        return ResponseEntity.ok(ApiResponse.success(rate, "Success"));
    }

    /**
     * Amount already paid against a bill — the edit screen refuses to save a
     * total below this, which would leave the supplier over-paid.
     * GET /api/bills/{id}/paid-amount?companyId=6
     *
     * <p>Legacy: {@code POST /BillMaster/CheckEditAmount}.
     */
    @GetMapping("/{id}/paid-amount")
    public ResponseEntity<ApiResponse<Double>> paidAmount(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        return ResponseEntity.ok(ApiResponse.success(
                transactions.paidAmount(companyId, id), "Success"));
    }

    /**
     * Whether a supplier invoice number is already used by another bill.
     * GET /api/bills/invoice-no-exists?companyId=6&invoiceNo=INV-99&excludeBillId=12
     *
     * <p>Legacy: {@code POST /BillMaster/CheckInvoiceNo}, which shipped every
     * invoice number in the company to the browser to search client-side.
     */
    @GetMapping("/invoice-no-exists")
    public ResponseEntity<ApiResponse<Boolean>> invoiceNoExists(
            @RequestParam Integer companyId,
            @RequestParam String invoiceNo,
            @RequestParam(required = false) Integer excludeBillId) {
        boolean exists = transactions.invoiceNoExists(companyId, invoiceNo, excludeBillId);
        return ResponseEntity.ok(ApiResponse.success(
                exists, exists ? "Invoice number already exists" : "Invoice number is available"));
    }

    /* ── QNE ───────────────────────────────────────────────────────── */

    /**
     * Push bill to QNE
     * POST /api/bills/{id}/push-qne?companyId=1
     *
     * Create-once via the empty-QNECode guard (legacy BillMasterConvert). A
     * QNE rejection answers 200 with IsSuccess=false and QNE's own message.
     */
    @PostMapping("/{id}/push-qne")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushToQne(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid ID or company ID", 400));
        }
        return QnePushResponses.toResponse(qneService.push(id, companyId));
    }

    /* ── plain CRUD (unchanged) ────────────────────────────────────── */

    @GetMapping
    public List<BillMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public BillMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<BillMasterDto> create(@Valid @RequestBody BillMasterDto dto) {
        BillMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/bills/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public BillMasterDto update(@PathVariable Integer id, @Valid @RequestBody BillMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/hard/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static Integer firstNonNull(Integer... values) {
        for (Integer value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
