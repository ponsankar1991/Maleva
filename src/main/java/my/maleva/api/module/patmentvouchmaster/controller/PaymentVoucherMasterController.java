package my.maleva.api.module.patmentvouchmaster.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherEditDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherF5ViewDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherMasterDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherComboResponse;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherSaveRequestDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherSaveResponseDto;
import my.maleva.api.module.patmentvouchmaster.dto.SelectPaymentVoucherRequestDto;
import my.maleva.api.module.patmentvouchmaster.service.PaymentVoucherMasterService;
import my.maleva.api.module.patmentvouchmaster.service.PaymentVoucherQneService;
import my.maleva.api.module.patmentvouchmaster.service.PaymentVoucherTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-voucher-masters")
@Validated
@PermitAll
public class PaymentVoucherMasterController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentVoucherMasterController.class);

    private final PaymentVoucherMasterService service;
    private final PaymentVoucherQneService qneService;
    private final PaymentVoucherTransactionService transactions;

    public PaymentVoucherMasterController(PaymentVoucherMasterService service,
                                          PaymentVoucherQneService qneService,
                                          PaymentVoucherTransactionService transactions) {
        this.service = service;
        this.qneService = qneService;
        this.transactions = transactions;
    }

    /* ── voucher screen ────────────────────────────────────────────── */

    /**
     * Next voucher number, for a blank screen.
     * GET /api/payment-voucher-masters/next-number?companyId=6
     *
     * <p>Legacy: {@code POST /PaymentVoucher/MaxPaymentVoucherNo}. Preview
     * only — the number is assigned when the voucher is saved.
     */
    @GetMapping("/next-number")
    public ResponseEntity<ApiResponse<String>> nextNumber(@RequestParam Integer companyId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.nextVoucherNumber(companyId), "Next voucher number generated"));
        } catch (Exception e) {
            logger.error("Error generating next voucher number for company {}", companyId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error generating voucher number: " + e.getMessage(), 500));
        }
    }

    /**
     * Save a voucher and its expense lines — insert when id is 0/absent,
     * otherwise update.
     * POST /api/payment-voucher-masters/save?companyId=6
     *
     * <p>Legacy: {@code POST /PaymentVoucher/InsertPaymentVoucher}, which
     * posted an array; this takes the single object it always contained.
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<PaymentVoucherSaveResponseDto>> save(
            @RequestBody PaymentVoucherSaveRequestDto dto,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        Integer company = companyId != null ? companyId : comid;
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Company ID is required", 400));
        }
        try {
            PaymentVoucherSaveResponseDto result = transactions.save(dto, company);
            if (!result.isSuccess()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(result.getMessage(), 400));
            }
            return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving payment voucher for company {}", company, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error saving payment voucher: " + e.getMessage(), 500));
        }
    }

    /**
     * Load one voucher for editing, by id or by its running number.
     * GET /api/payment-voucher-masters/edit?companyId=6&id=12
     * GET /api/payment-voucher-masters/edit?companyId=6&voucherNumber=45
     *
     * <p>Legacy: {@code POST /PaymentVoucher/EditPaymentVoucher}.
     */
    @GetMapping("/edit")
    public ResponseEntity<ApiResponse<PaymentVoucherEditDto>> edit(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer voucherNumber) {
        if ((id == null || id == 0) && (voucherNumber == null || voucherNumber == 0)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Provide either id or voucherNumber", 400));
        }
        try {
            return transactions.edit(id, voucherNumber, companyId)
                    .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Voucher loaded")))
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(ApiResponse.error("Invaild Payment Voucher No !!!.", 404)));
        } catch (Exception e) {
            logger.error("Error loading voucher id={} number={}", id, voucherNumber, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error loading voucher: " + e.getMessage(), 500));
        }
    }

    /**
     * The F5 search grid: vouchers plus their expense lines and the total.
     * POST /api/payment-voucher-masters/search?companyId=6
     *
     * <p>Legacy: {@code POST /PaymentVoucher/SelectPaymentVoucherMaster}.
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaymentVoucherF5ViewDto>> search(
            @RequestBody SelectPaymentVoucherRequestDto request,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        Integer company = companyId != null ? companyId
                : comid != null ? comid : request.getComid();
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Company ID is required", 400));
        }
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.search(request, company), "Success"));
        } catch (Exception e) {
            logger.error("Error searching vouchers for company {}", company, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error searching vouchers: " + e.getMessage(), 500));
        }
    }

    /**
     * Descriptions used on earlier vouchers, for the description dropdown.
     * GET /api/payment-voucher-masters/descriptions?companyId=6
     *
     * <p>Legacy: {@code POST /PaymentVoucher/SelectDescription}.
     */
    @GetMapping("/descriptions")
    public ResponseEntity<ApiResponse<List<String>>> descriptions(@RequestParam Integer companyId) {
        return ResponseEntity.ok(ApiResponse.success(
                transactions.descriptions(companyId), "Success"));
    }

    /**
     * Mark a voucher as cleared.
     * POST /api/payment-voucher-masters/{id}/complete?companyId=6
     *
     * <p>Legacy: {@code POST /PaymentVoucher/UpdatePaymentVoucherStatus},
     * which set the status without checking the company.
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> markCompleted(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        try {
            if (!transactions.markCompleted(id, companyId)) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Payment voucher not found: " + id, 404));
            }
            return ResponseEntity.ok(ApiResponse.success(null, "Payment voucher marked as completed"));
        } catch (Exception e) {
            logger.error("Error completing voucher {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating voucher status: " + e.getMessage(), 500));
        }
    }

    /**
     * Push payment voucher to QNE
     * POST /api/payment-voucher-masters/{id}/push-qne?companyId=1
     *
     * Create-once via the empty-QNECode guard (legacy PaymentVoucherConvert).
     * A QNE rejection answers 200 with IsSuccess=false and QNE's own message.
     */
    @PostMapping("/{id}/push-qne")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushToQne(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Pushing payment voucher ID: {} to QNE for company: {}", id, companyId);
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid ID or company ID", 400));
        }
        return QnePushResponses.toResponse(qneService.push(id, companyId));
    }

    @GetMapping
    public List<PaymentVoucherMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PaymentVoucherMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PaymentVoucherMasterDto> create(@Valid @RequestBody PaymentVoucherMasterDto dto) {
        PaymentVoucherMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/payment-voucher-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PaymentVoucherMasterDto update(@PathVariable Integer id, @Valid @RequestBody PaymentVoucherMasterDto dto) {
        return service.update(id, dto);
    }

    /**
     * Soft-delete a voucher ({@code Active=2}, as legacy did — payments and
     * the QNE trail still reference it). Refused once the voucher is in QNE.
     *
     * <p>This replaces an earlier hard delete that removed the row outright
     * and checked no company. {@code companyId} is now required.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        Integer company = companyId != null ? companyId : comid;
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Company ID is required", 400));
        }
        try {
            if (!transactions.delete(id, company)) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Payment voucher not found: " + id, 404));
            }
            return ResponseEntity.ok(ApiResponse.success(null, "PaymentVoucher deleted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(ApiResponse.error(e.getMessage(), 409));
        } catch (Exception e) {
            logger.error("Error deleting voucher {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error deleting voucher: " + e.getMessage(), 500));
        }
    }

    /**
     * SelectPaymentTo - Get distinct PayTo values for a company
     * Equivalent to .NET SelectPaymentTo method from PaymentVoucherServices
     * HTTP: GET /api/payment-voucher-masters/select-payment-to
     * Header: Comid (Company ID) OR Query Parameter: ?comid=6
     * Response: { "ok": true/false, "message": "...", "data": [...] }
     * Example: GET /api/payment-voucher-masters/select-payment-to?comid=6
     * @param comid Company ID passed as request parameter
     * @return PaymentVoucherComboResponse with distinct PayTo values
     */
    @GetMapping("/select-payment-to")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<PaymentVoucherComboResponse> selectPaymentTo(
            @RequestParam(value = "comid", required = false) Integer comid) {

        logger.info("SelectPaymentTo endpoint called - comid: {}", comid);

        try {
            if (comid == null || comid <= 0) {
                logger.warn("Invalid request: Comid is missing or invalid: {}", comid);
                PaymentVoucherComboResponse response = PaymentVoucherComboResponse.error("Company ID (Comid) is required");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("Calling service for company: {}", comid);
            PaymentVoucherComboResponse response = service.selectPaymentTo(comid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in SelectPaymentTo endpoint", e);
            PaymentVoucherComboResponse response = PaymentVoucherComboResponse.error("Error retrieving PayTo values: " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * SelectPaymentFrom - Get distinct PayFrom values for a company
     * Equivalent to .NET SelectPaymentFrom method from PaymentVoucherServices
     * HTTP: POST /api/payment-voucher-masters/select-payment-from
     * Header: Comid (Company ID) OR Query Parameter: ?comid=6
     * Response: { "ok": true/false, "message": "...", "data": [...] }
     * Example: POST /api/payment-voucher-masters/select-payment-from?comid=6
     * @param comid Company ID passed as request parameter
     * @return PaymentVoucherComboResponse with distinct PayFrom values
     */
    @GetMapping("/select-payment-from")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<PaymentVoucherComboResponse> selectPaymentFrom(
            @RequestParam(value = "comid", required = false) Integer comid) {

        logger.info("SelectPaymentFrom endpoint called - comid: {}", comid);

        try {
            if (comid == null || comid <= 0) {
                logger.warn("Invalid request: Comid is missing or invalid: {}", comid);
                PaymentVoucherComboResponse response = PaymentVoucherComboResponse.error("Company ID (Comid) is required");
                return ResponseEntity.badRequest().body(response);
            }
            logger.info("Calling service for company: {}", comid);
            PaymentVoucherComboResponse response = service.selectPaymentFrom(comid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in SelectPaymentFrom endpoint", e);
            PaymentVoucherComboResponse response = PaymentVoucherComboResponse.error("Error retrieving PayFrom values: " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
