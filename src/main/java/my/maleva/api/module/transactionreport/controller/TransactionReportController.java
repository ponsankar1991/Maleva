package my.maleva.api.module.transactionreport.controller;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.transactionreport.dto.PaymentDoneRequestDto;
import my.maleva.api.module.transactionreport.dto.PaymentDoneViewDto;
import my.maleva.api.module.transactionreport.service.TransactionReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Transaction reports.
 *
 * <p>Replaces the legacy {@code Controllers/Report/TransactionReportController}
 * one action at a time; today it serves the Payment Completed screen.
 */
@Slf4j
@RestController
@RequestMapping("/api/transaction-reports")
@Validated
@RequiredArgsConstructor
@PermitAll
public class TransactionReportController {

    private final TransactionReportService service;

    /**
     * The Payment Completed grid.
     *
     * <p>{@code POST /api/transaction-reports/payment-done?companyId=6}.
     * Replaces legacy {@code POST /TransactionReport/SelectPaymentDone}.
     *
     * <p>A POST rather than a GET because the category filter is a list of up
     * to fourteen names; the company id is accepted three ways (query param,
     * {@code Comid} header, request body) for the same reason the petty cash
     * search does — callers migrated from the legacy screens send the header.
     */
    @PostMapping("/payment-done")
    public ResponseEntity<ApiResponse<PaymentDoneViewDto>> paymentDone(
            @RequestBody PaymentDoneRequestDto request,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {

        Integer company = companyId != null ? companyId
                : comid != null ? comid : request.getComid();
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Company ID is required", 400));
        }
        request.setComid(company);

        try {
            return ResponseEntity.ok(ApiResponse.success(
                    service.getCompletedPayments(request), "Success"));
        } catch (Exception e) {
            log.error("Error loading completed payments for company {}", company, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error loading completed payments: " + e.getMessage(), 500));
        }
    }
}
