package my.maleva.api.module.payment.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.payment.dto.PaymentEditDto;
import my.maleva.api.module.payment.dto.PaymentF5ViewDto;
import my.maleva.api.module.payment.dto.PaymentMasterViewDto;
import my.maleva.api.module.payment.dto.PaymentSaveRequestDto;
import my.maleva.api.module.payment.dto.PaymentSaveResponseDto;
import my.maleva.api.module.payment.dto.SelectPaymentRequestDto;
import my.maleva.api.module.payment.dto.SupplierBalanceDto;
import my.maleva.api.module.payment.dto.SupplierBillDto;
import my.maleva.api.module.payment.service.PaymentQneService;
import my.maleva.api.module.payment.service.PaymentTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pay Bills screen API — the Java replacement for the legacy
 * {@code /Payment/*} MVC endpoints.
 *
 * <p>Company scope arrives as {@code companyId} (query parameter) or the
 * {@code Comid} header, matching how the rest of this API is called.
 *
 * <p>Attachments are not here: bill and payment documents stay on the .NET host
 * ({@code /Common/UploadFile2}, folder {@code PayBills}), and the {@code id}
 * this API returns from a save is what the upload attaches to.
 */
@RestController
@RequestMapping("/api/payments")
@Validated
@PermitAll
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentTransactionService transactions;
    private final PaymentQneService qneService;

    public PaymentController(PaymentTransactionService transactions,
                             PaymentQneService qneService) {
        this.transactions = transactions;
        this.qneService = qneService;
    }

    /* ── payment screen ────────────────────────────────────────────── */

    /**
     * Next payment number, for a blank screen.
     * GET /api/payments/next-number?companyId=6
     *
     * <p>Legacy: {@code POST /Payment/MaxPaymentNo}. A preview only — the
     * number is assigned when the payment is saved.
     */
    @GetMapping("/next-number")
    public ResponseEntity<ApiResponse<String>> nextNumber(@RequestParam Integer companyId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.nextPaymentNumber(companyId), "Next payment number generated"));
        } catch (Exception e) {
            logger.error("Error generating next payment number for company {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error generating payment number: " + e.getMessage(), 500));
        }
    }

    /**
     * Save a payment and the documents it settles — insert when id is
     * 0/absent, otherwise update.
     * POST /api/payments/save?companyId=6
     *
     * <p>Legacy: {@code POST /Payment/InsertPayment}, which posted an array;
     * this takes the single object it always contained.
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<PaymentSaveResponseDto>> save(
            @Valid @RequestBody PaymentSaveRequestDto dto,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        Integer company = firstNonNull(companyId, comid);
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Company ID is required", 400));
        }
        try {
            PaymentSaveResponseDto result = transactions.save(dto, company);
            if (!result.isSuccess()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(result.getMessage(), 400));
            }
            return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving payment for company {}", company, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error saving payment: " + e.getMessage(), 500));
        }
    }

    /**
     * Load one payment for editing, by id or by its running number.
     * GET /api/payments/edit?companyId=6&id=12
     * GET /api/payments/edit?companyId=6&paymentNumber=45
     *
     * <p>Legacy: {@code POST /Payment/EditPayment}. The returned
     * {@code paymentDetails} is the supplier's whole outstanding list with this
     * payment's amounts written back in, not just the saved lines.
     */
    @GetMapping("/edit")
    public ResponseEntity<ApiResponse<PaymentEditDto>> edit(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer paymentNumber) {
        if ((id == null || id == 0) && (paymentNumber == null || paymentNumber == 0)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Provide either id or paymentNumber", 400));
        }
        try {
            Optional<PaymentEditDto> payment = transactions.edit(id, paymentNumber, companyId);
            return payment
                    .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Payment loaded")))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Invalid Payment No", 404)));
        } catch (Exception e) {
            logger.error("Error loading payment id={} number={}", id, paymentNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error loading payment: " + e.getMessage(), 500));
        }
    }

    /**
     * The F5 search grid: payments plus the documents each one settles.
     * POST /api/payments/search?companyId=6
     *
     * <p>Legacy: {@code POST /Payment/SelectPayment}. Dates are
     * {@code yyyy-MM-dd} (or {@code dd/MM/yyyy}); a non-empty {@code search}
     * matches the payment number and ignores every other filter.
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaymentF5ViewDto>> search(
            @RequestBody SelectPaymentRequestDto request,
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
            logger.error("Error searching payments for company {}", company, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error searching payments: " + e.getMessage(), 500));
        }
    }

    /**
     * Delete a payment and the settlements beneath it.
     * DELETE /api/payments/{id}?companyId=6
     *
     * <p>Legacy: {@code POST /Payment/DeletePayment}. Refused once the payment
     * has been pushed to the payment-voucher queue.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        try {
            if (!transactions.delete(id, companyId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Payment not found: " + id, 404));
            }
            return ResponseEntity.ok(ApiResponse.success(null, "Payment deleted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage(), 409));
        } catch (Exception e) {
            logger.error("Error deleting payment {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting payment: " + e.getMessage(), 500));
        }
    }

    /* ── screen lookups ────────────────────────────────────────────── */

    /**
     * Everything a supplier is still owed money on, for the payment grid.
     * GET /api/payments/supplier-bills?companyId=6&supplierId=12
     *
     * <p>Legacy: {@code POST /Payment/SelectSupplierBills} ({@code RT_SupplierBills}).
     * Pass {@code excludePaymentId} when reopening a payment so the bills it
     * settles still show as outstanding.
     */
    @GetMapping("/supplier-bills")
    public ResponseEntity<ApiResponse<List<SupplierBillDto>>> supplierBills(
            @RequestParam Integer companyId,
            @RequestParam Integer supplierId,
            @RequestParam(required = false) Integer excludePaymentId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.supplierBills(companyId, supplierId, excludePaymentId), "Success"));
        } catch (Exception e) {
            logger.error("Error loading outstanding bills for supplier {}", supplierId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error loading supplier bills: " + e.getMessage(), 500));
        }
    }

    /**
     * A supplier's running balance as at a date; omit {@code supplierId} for
     * every active supplier.
     * GET /api/payments/supplier-balance?companyId=6&supplierId=12&tillDate=2026-08-27
     *
     * <p>Legacy: {@code POST /Payment/SelectSupplierBalance}.
     */
    @GetMapping("/supplier-balance")
    public ResponseEntity<ApiResponse<List<SupplierBalanceDto>>> supplierBalance(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(required = false) String tillDate) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.supplierBalance(companyId, supplierId, tillDate), "Success"));
        } catch (Exception e) {
            logger.error("Error loading balance for supplier {}", supplierId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error loading supplier balance: " + e.getMessage(), 500));
        }
    }

    /**
     * The credit period on a payment term, in days — the screen adds it to the
     * payment date to show a due date.
     * GET /api/payments/payment-terms-due?paymentTermsId=3
     *
     * <p>Legacy: {@code POST /Payment/SelectSupplierDue}, whose
     * {@code SupplierId} parameter actually carried a payment-terms id.
     */
    @GetMapping("/payment-terms-due")
    public ResponseEntity<ApiResponse<Integer>> paymentTermsDue(
            @RequestParam Integer paymentTermsId) {
        return ResponseEntity.ok(ApiResponse.success(
                transactions.paymentTermsDueDays(paymentTermsId), "Success"));
    }

    /**
     * Payments still waiting to go onward, optionally only those QNE has never
     * seen.
     * GET /api/payments/due?companyId=6&fromDate=2026-08-01&toDate=2026-08-31&dateWise=true
     *
     * <p>Legacy: {@code POST /Payment/SelectDueBills}.
     */
    @GetMapping("/due")
    public ResponseEntity<ApiResponse<List<PaymentMasterViewDto>>> dueBills(
            @RequestParam Integer companyId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(defaultValue = "false") boolean dateWise,
            @RequestParam(defaultValue = "false") boolean notInQne) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.dueBills(companyId, fromDate, toDate, employeeId, dateWise, notInQne),
                    "Success"));
        } catch (Exception e) {
            logger.error("Error fetching due payments for company {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error fetching due payments: " + e.getMessage(), 500));
        }
    }

    /**
     * Mark a payment as cleared.
     * POST /api/payments/{id}/complete?companyId=6
     *
     * <p>Legacy: {@code POST /Payment/Updatepaymentstatus}, which set
     * PaymentStatus to COMPLETED without checking the company.
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> markCompleted(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        try {
            if (!transactions.markCompleted(id, companyId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Payment not found: " + id, 404));
            }
            return ResponseEntity.ok(ApiResponse.success(null, "Payment marked as completed"));
        } catch (Exception e) {
            logger.error("Error completing payment {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating payment status: " + e.getMessage(), 500));
        }
    }

    /* ── QNE ───────────────────────────────────────────────────────── */

    /**
     * Push payment to QNE PayBills
     * POST /api/payments/{id}/push-qne?companyId=1
     *
     * Create-once via the empty-QNECode guard (legacy PaymentConvert). A QNE
     * rejection answers 200 with IsSuccess=false and QNE's own message.
     */
    @PostMapping("/{id}/push-qne")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushToQne(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Pushing payment ID: {} to QNE for company: {}", id, companyId);
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid ID or company ID", 400));
        }
        return QnePushResponses.toResponse(qneService.push(id, companyId));
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
