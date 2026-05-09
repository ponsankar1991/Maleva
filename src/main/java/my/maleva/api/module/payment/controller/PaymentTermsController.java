package my.maleva.api.module.payment.controller;

import my.maleva.api.common.dto.ResponseViewModel;
import my.maleva.api.module.payment.service.PaymentTermsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PaymentTermsController - REST Controller for Payment Terms API
 *
 * Endpoints:
 * - GET /api/payment-terms/supplier-due - Select supplier payment terms (TDays)
 * - GET /api/payment-terms/{id} - Get payment terms by ID
 * - POST /api/payment-terms/calculate-due-date - Calculate payment due date
 *
 * Architecture: Controller layer handles HTTP requests/responses
 * Service layer handles business logic
 * Repository layer handles database access
 */
@RestController
@RequestMapping("/api/payment-terms")
public class PaymentTermsController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentTermsController.class);

    @Autowired
    private PaymentTermsService paymentTermsService;

    /**
     * Select Supplier Payment Terms (TDays)
     *
     * Equivalent to .NET: SelectSupplierDue(int Comid, int SupplierId)
     *
     * GET /api/payment-terms/supplier-due?comid=1&supplierId=5
     *
     * Request Parameters:
     * - comid: Company ID (for context)
     * - supplierId: Supplier ID (references PaymentTermsMaster.Id)
     *
     * Response: ResponseViewModel
     * {
     *   "ok": true/false,
     *   "message": "Success or error message",
     *   "data": { id, termsName, tDays, active }
     * }
     *
     * @param comid Company Reference ID
     * @param supplierId Supplier ID
     * @return ResponseEntity with ResponseViewModel containing payment terms data
     */
    @GetMapping("/supplier-due")
    public ResponseEntity<?> selectSupplierDue(
            @RequestParam(value = "comid", required = false) Integer comid,
            @RequestParam(value = "supplierId", required = false) Integer supplierId) {

        logger.info("API Call: selectSupplierDue - comid: {}, supplierId: {}", comid, supplierId);

        try {
            // Validate required parameters
            if (supplierId == null || supplierId <= 0) {
                logger.warn("Invalid request: supplierId is missing or invalid");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseViewModel.error("Supplier ID is required and must be greater than 0", 400));
            }

            // Call service to fetch payment terms
            ResponseViewModel response = paymentTermsService.selectSupplierDue(comid, supplierId);

            // Return appropriate HTTP status based on service response
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                int statusCode = response.getStatusCode() != null ? response.getStatusCode() : 400;
                return ResponseEntity.status(statusCode).body(response);
            }

        } catch (Exception ex) {
            logger.error("Error in selectSupplierDue endpoint", ex);
            ResponseViewModel errorResponse = ResponseViewModel.error(
                    "Internal server error: " + ex.getMessage(),
                    500
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get Payment Terms by ID
     *
     * GET /api/payment-terms/5
     *
     * @param id Payment Terms Master ID
     * @return ResponseEntity with complete payment terms details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentTermsById(@PathVariable Integer id) {

        logger.info("API Call: getPaymentTermsById - id: {}", id);

        try {
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseViewModel.error("Invalid Payment Terms ID", 400));
            }

            ResponseViewModel response = paymentTermsService.getPaymentTermsById(id);

            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                int statusCode = response.getStatusCode() != null ? response.getStatusCode() : 400;
                return ResponseEntity.status(statusCode).body(response);
            }

        } catch (Exception ex) {
            logger.error("Error in getPaymentTermsById endpoint", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseViewModel.error("Internal server error: " + ex.getMessage(), 500));
        }
    }

    /**
     * Calculate Payment Due Date
     *
     * POST /api/payment-terms/calculate-due-date
     *
     * Request Parameters:
     * - paymentTermsId: Payment Terms Master ID
     * - invoiceDate: Invoice date in format (yyyy-MM-dd)
     *
     * Response:
     * {
     *   "isSuccess": true,
     *   "statusCode": 200,
     *   "message": "Due date calculated successfully: 2026-06-07",
     *   "data1": "2026-06-07"
     * }
     *
     * @param paymentTermsId Payment Terms ID
     * @param invoiceDate Invoice date
     * @return ResponseEntity with calculated due date
     */
    @PostMapping("/calculate-due-date")
    public ResponseEntity<?> calculatePaymentDueDate(
            @RequestParam(value = "paymentTermsId", required = false) Integer paymentTermsId,
            @RequestParam(value = "invoiceDate", required = false) String invoiceDate) {

        logger.info("API Call: calculatePaymentDueDate - paymentTermsId: {}, invoiceDate: {}", paymentTermsId, invoiceDate);

        try {
            if (paymentTermsId == null || paymentTermsId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseViewModel.error("Payment Terms ID is required", 400));
            }

            if (invoiceDate == null || invoiceDate.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseViewModel.error("Invoice date is required", 400));
            }

            ResponseViewModel response = paymentTermsService.calculatePaymentDueDate(paymentTermsId, invoiceDate);

            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                int statusCode = response.getStatusCode() != null ? response.getStatusCode() : 400;
                return ResponseEntity.status(statusCode).body(response);
            }

        } catch (Exception ex) {
            logger.error("Error in calculatePaymentDueDate endpoint", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseViewModel.error("Internal server error: " + ex.getMessage(), 500));
        }
    }

    /**
     * Alternative endpoint for selectSupplierDue using POST (matches some legacy systems)
     *
     * POST /api/payment-terms/select-supplier-due
     *
     * Request Body (form-data or params):
     * - comid: Company ID
     * - supplierId: Supplier ID
     *
     * @param comid Company ID
     * @param supplierId Supplier ID
     * @return ResponseEntity with payment terms data
     */
    @PostMapping("/select-supplier-due")
    public ResponseEntity<?> selectSupplierDuePost(
            @RequestParam(value = "comid", required = false) Integer comid,
            @RequestParam(value = "supplierId", required = false) Integer supplierId) {

        logger.info("API Call (POST): selectSupplierDue - comid: {}, supplierId: {}", comid, supplierId);
        return selectSupplierDue(comid, supplierId);
    }
}

