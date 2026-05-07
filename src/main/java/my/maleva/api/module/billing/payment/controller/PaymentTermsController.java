package my.maleva.api.module.billing.payment.controller;

import my.maleva.api.module.billing.payment.service.PaymentService;
import my.maleva.api.common.dto.ResponseViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * PaymentTermsController - REST Controller for Payment Terms API
 * Handles payment terms and supplier payment-related operations
 */
@RestController
@RequestMapping("/api/payment")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PaymentTermsController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentTermsController.class);

    private final PaymentService paymentService;

    public PaymentTermsController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Select Supplier Due - Get Payment Terms Days
     * GET /api/payment/select-supplier-due
     *
     * Retrieves payment terms days (TDays) from PaymentTermsMaster
     * Equivalent to .NET SelectSupplierDue method
     * Calculates how many days the supplier has to pay
     *
     * @param companyId      The Company Reference ID
     * @param paymentTermId  The Payment Terms Master ID
     * @return ResponseViewModel with TDays in data1
     */
    @GetMapping("/select-supplier-due")
    public ResponseEntity<ResponseViewModel> selectSupplierDue(
            @RequestParam(value = "companyId") Integer companyId,
            @RequestParam(value = "paymentTermId") Integer paymentTermId) {
        logger.info("Fetching supplier due - company: {}, payment term ID: {}", companyId, paymentTermId);

        try {
            ResponseViewModel response = paymentService.selectSupplierDue(companyId, paymentTermId);

            if (response.isSuccess()) {
                logger.info("Successfully retrieved supplier due - TDays: {} for payment term: {}",
                           response.getData1(), paymentTermId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Failed to retrieve supplier due for payment term: {}", paymentTermId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception ex) {
            logger.error("Error fetching supplier due for company: {}, payment term ID: {}",
                        companyId, paymentTermId, ex);

            ResponseViewModel errorResponse = ResponseViewModel.builder()
                    .isSuccess(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(ex.getMessage() != null ? ex.getMessage() : "Error fetching supplier due")
                    .data1("Api Details: Payment_SelectSupplierDue")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get Payment Terms Details
     * GET /api/payment/terms/{paymentTermId}
     *
     * Retrieves complete payment terms details by ID
     *
     * @param paymentTermId The Payment Terms Master ID
     * @return ResponseViewModel with PaymentTermsDto in data1
     */
    @GetMapping("/terms/{paymentTermId}")
    public ResponseEntity<ResponseViewModel> getPaymentTermsById(@PathVariable Integer paymentTermId) {
        logger.info("Fetching payment terms details for ID: {}", paymentTermId);

        try {
            ResponseViewModel response = paymentService.getPaymentTermsById(paymentTermId);

            if (response.isSuccess()) {
                logger.info("Successfully retrieved payment terms for ID: {}", paymentTermId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Payment terms not found for ID: {}", paymentTermId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception ex) {
            logger.error("Error fetching payment terms for ID: {}", paymentTermId, ex);

            ResponseViewModel errorResponse = ResponseViewModel.builder()
                    .isSuccess(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(ex.getMessage() != null ? ex.getMessage() : "Error fetching payment terms")
                    .data1("Api Details: Payment_GetPaymentTermsById")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get TDays Value Only
     * GET /api/payment/tdays/{paymentTermId}
     *
     * Simplified endpoint to retrieve just the number of payment days
     *
     * @param paymentTermId The Payment Terms Master ID
     * @return ResponseViewModel with TDays integer in data1
     */
    @GetMapping("/tdays/{paymentTermId}")
    public ResponseEntity<ResponseViewModel> getTDaysById(@PathVariable Integer paymentTermId) {
        logger.info("Fetching TDays for payment term ID: {}", paymentTermId);

        try {
            Integer tDays = paymentService.getTDaysById(paymentTermId);

            if (tDays > 0) {
                ResponseViewModel response = ResponseViewModel.builder()
                        .isSuccess(true)
                        .statusCode(HttpStatus.OK.value())
                        .message("TDays retrieved successfully")
                        .data1(tDays)
                        .build();

                logger.info("Successfully retrieved TDays: {} for payment term ID: {}", tDays, paymentTermId);
                return ResponseEntity.ok(response);

            } else {
                ResponseViewModel response = ResponseViewModel.builder()
                        .isSuccess(false)
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message("Payment terms not found for ID: " + paymentTermId)
                        .data1("Api Details: Payment_GetTDaysById")
                        .build();

                logger.warn("Payment terms not found for ID: {}", paymentTermId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception ex) {
            logger.error("Error fetching TDays for payment term ID: {}", paymentTermId, ex);

            ResponseViewModel errorResponse = ResponseViewModel.builder()
                    .isSuccess(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(ex.getMessage() != null ? ex.getMessage() : "Error fetching TDays")
                    .data1("Api Details: Payment_GetTDaysById")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Calculate Payment Due Date
     * GET /api/payment/calculate-due-date/{paymentTermId}
     *
     * Calculates payment due days - same as selectSupplierDue
     *
     * @param paymentTermId The Payment Terms Master ID
     * @return ResponseViewModel with TDays in data1
     */
    @GetMapping("/calculate-due-date/{paymentTermId}")
    public ResponseEntity<ResponseViewModel> calculatePaymentDueDate(@PathVariable Integer paymentTermId) {
        logger.info("Calculating payment due date for term ID: {}", paymentTermId);

        try {
            ResponseViewModel response = paymentService.calculatePaymentDueDate(paymentTermId);

            if (response.isSuccess()) {
                logger.info("Successfully calculated payment due date - TDays: {} for term ID: {}",
                           response.getData1(), paymentTermId);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Failed to calculate payment due date for term ID: {}", paymentTermId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception ex) {
            logger.error("Error calculating payment due date for term ID: {}", paymentTermId, ex);

            ResponseViewModel errorResponse = ResponseViewModel.builder()
                    .isSuccess(false)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(ex.getMessage() != null ? ex.getMessage() : "Error calculating payment due date")
                    .data1("Api Details: Payment_CalculateDueDate")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

