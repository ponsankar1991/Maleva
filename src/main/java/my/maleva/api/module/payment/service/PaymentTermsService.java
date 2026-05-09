package my.maleva.api.module.payment.service;

import my.maleva.api.common.dto.ResponseViewModel;

/**
 * PaymentTermsService Interface
 * Handles payment terms related business logic and database queries
 */
public interface PaymentTermsService {

    /**
     * Select Supplier Payment Terms by Supplier ID
     *
     * Query: Select TDays from PaymentTermsMaster where Id = SupplierId
     *
     * @param comid Company ID (for context, though not directly used in query)
     * @param supplierId Supplier ID (references PaymentTermsMaster.Id)
     * @return ResponseViewModel containing payment terms data with TDays
     */
    ResponseViewModel selectSupplierDue(Integer comid, Integer supplierId);

    /**
     * Get Payment Terms by ID
     *
     * @param paymentTermsId Payment Terms Master ID
     * @return ResponseViewModel with payment terms details
     */
    ResponseViewModel getPaymentTermsById(Integer paymentTermsId);

    /**
     * Calculate Payment Due Date based on TDays
     *
     * @param paymentTermsId Payment Terms Master ID
     * @param invoiceDate Base invoice date
     * @return ResponseViewModel with calculated due date
     */
    ResponseViewModel calculatePaymentDueDate(Integer paymentTermsId, String invoiceDate);
}

