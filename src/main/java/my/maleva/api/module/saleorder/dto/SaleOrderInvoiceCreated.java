package my.maleva.api.module.saleorder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * Outcome of Create Invoice. The invoice part is always filled once the
 * procedure has accepted it; the status part says whether the sale order was
 * also moved to its completed status, and why not when it was not.
 */
public record SaleOrderInvoiceCreated(
        Integer saleOrderId,
        String jobNo,
        Integer invoiceId,
        String invoiceNo,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate invoiceDate,
        Double netTotal,
        Integer statusId,
        String statusName,
        boolean statusUpdated,
        String statusMessage) {
}
