package my.maleva.api.module.payment.service.impl;

import my.maleva.api.common.dto.ResponseViewModel;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.payment.dto.SupplierPaymentTermsDto;
import my.maleva.api.module.payment.service.PaymentTermsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * PaymentTermsService Implementation
 *
 * Handles payment terms related operations:
 * - Fetching TDays (payment terms) by supplier ID
 * - Calculating payment due dates
 * - Retrieving payment terms details
 *
 * Architecture: Service layer handles business logic and validation
 * Repository layer handles database access
 */
@Service
@Transactional
public class PaymentTermsServiceImpl implements PaymentTermsService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentTermsServiceImpl.class);

    @Autowired
    private PaymentTermsMasterRepository paymentTermsRepository;

    /**
     * Select Supplier Payment Terms by Supplier ID
     *
     * Equivalent C# Query: SELECT PM.TDays FROM PaymentTermsMaster PM WHERE PM.Id = SupplierId
     * Equivalent C# Code: _dapper.ExecuteScalar(query)
     *
     * Business Logic:
     * 1. Fetch PaymentTermsMaster record by ID
     * 2. Extract TDays value (payment term days)
     * 3. Return TDays wrapped in ResponseViewModel with status code and message
     *
     * @param comid Company ID (for context/logging)
     * @param supplierId Supplier ID (references PaymentTermsMaster.Id)
     * @return ResponseViewModel containing:
     * - isSuccess: true/false
     * - statusCode: 200 (Success), 404 (Not Found), 500 (Error)
     * - message: Success or error message
     * - data1: Integer TDays value (number of days for payment terms)
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseViewModel selectSupplierDue(Integer comid, Integer supplierId) {
        logger.info("Fetching payment terms TDays for supplierId: {} from company: {}", supplierId, comid);

        try {
            // Validate input parameters
            if (supplierId == null || supplierId <= 0) {
                logger.warn("Invalid supplierId: {}", supplierId);
                return ResponseViewModel.error(
                        "Invalid Supplier ID provided",
                        400
                );
            }

            // Query database for PaymentTermsMaster record
            // Equivalent to: SELECT PM.TDays FROM PaymentTermsMaster PM WHERE PM.Id = SupplierId
            Optional<PaymentTermsMaster> paymentTerms = paymentTermsRepository.findById(supplierId);

            if (paymentTerms.isEmpty()) {
                logger.warn("Payment terms not found for supplierId: {}", supplierId);
                return ResponseViewModel.error(
                        "Payment terms not found for the given Supplier ID",
                        404
                );
            }

            // Extract TDays value from the entity
            PaymentTermsMaster terms = paymentTerms.get();
            Integer tDays = terms.getTDays();

            logger.info("Successfully retrieved TDays: {} for supplierId: {}", tDays, supplierId);

            // Return success response with TDays value (matching C# ExecuteScalar behavior)
            return ResponseViewModel.success(
                    tDays,
                    "Payment terms retrieved successfully",
                    200
            );

        } catch (Exception ex) {
            logger.error("Error fetching payment terms for supplierId: " + supplierId, ex);
            return ResponseViewModel.error(
                    "Error retrieving payment terms: " + ex.getMessage(),
                    500
            );
        }
    }

    /**
     * Get full Payment Terms Master record by ID
     *
     * @param paymentTermsId Payment Terms Master ID
     * @return ResponseViewModel with complete payment terms entity
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseViewModel getPaymentTermsById(Integer paymentTermsId) {
        logger.info("Fetching payment terms details for ID: {}", paymentTermsId);

        try {
            if (paymentTermsId == null || paymentTermsId <= 0) {
                return ResponseViewModel.error("Invalid Payment Terms ID", 400);
            }

            Optional<PaymentTermsMaster> paymentTerms = paymentTermsRepository.findById(paymentTermsId);

            if (paymentTerms.isEmpty()) {
                logger.warn("Payment terms not found for ID: {}", paymentTermsId);
                return ResponseViewModel.error("Payment terms not found", 404);
            }

            PaymentTermsMaster terms = paymentTerms.get();

            SupplierPaymentTermsDto dto = SupplierPaymentTermsDto.builder()
                    .id(terms.getId())
                    .termsName(terms.getTermsName())
                    .tDays(terms.getTDays())
                    .active(terms.getActive())
                    .build();

            return ResponseViewModel.success(dto, "Payment terms retrieved successfully", 200);

        } catch (Exception ex) {
            logger.error("Error fetching payment terms for ID: " + paymentTermsId, ex);
            return ResponseViewModel.error("Error: " + ex.getMessage(), 500);
        }
    }

    /**
     * Calculate Payment Due Date based on TDays
     *
     * Formula: Due Date = Invoice Date + TDays
     *
     * @param paymentTermsId Payment Terms Master ID
     * @param invoiceDate Base invoice date (format: yyyy-MM-dd)
     * @return ResponseViewModel with calculated due date
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseViewModel calculatePaymentDueDate(Integer paymentTermsId, String invoiceDate) {
        logger.info("Calculating payment due date for termId: {} with invoiceDate: {}", paymentTermsId, invoiceDate);

        try {
            // Validate inputs
            if (paymentTermsId == null || paymentTermsId <= 0) {
                return ResponseViewModel.error("Invalid Payment Terms ID", 400);
            }

            if (invoiceDate == null || invoiceDate.trim().isEmpty()) {
                return ResponseViewModel.error("Invalid invoice date provided", 400);
            }

            // Get payment terms
            Optional<PaymentTermsMaster> paymentTerms = paymentTermsRepository.findById(paymentTermsId);

            if (paymentTerms.isEmpty()) {
                return ResponseViewModel.error("Payment terms not found", 404);
            }

            PaymentTermsMaster terms = paymentTerms.get();
            Integer tDays = terms.getTDays();

            // Parse invoice date and calculate due date
            LocalDate invoiceDateParsed = LocalDate.parse(invoiceDate);
            LocalDate dueDate = invoiceDateParsed.plusDays(tDays);

            logger.info("Calculated due date: {} for invoice date: {} with TDays: {}", dueDate, invoiceDate, tDays);

            // Return calculated date
            return ResponseViewModel.success(
                    dueDate.toString(),
                    "Due date calculated successfully: " + dueDate,
                    200
            );

        } catch (Exception ex) {
            logger.error("Error calculating payment due date", ex);
            return ResponseViewModel.error("Error: " + ex.getMessage(), 500);
        }
    }
}

