package my.maleva.api.module.saleorder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * What the Create Invoice window shows before anything is written: the
 * invoice exactly as it will be saved from the sale order, plus the reasons
 * it cannot be created yet, if any.
 *
 * @param existing        the invoice that already bills this job, or null
 * @param canCreate       false when {@link #blockers} is non-empty or an invoice exists
 * @param blockers        problems that stop creation (no lines, no customer, ...)
 * @param warnings        things worth a look that do not stop creation
 * @param currentStatus   the sale order's job status today
 * @param completedStatus the status the sale order moves to once invoiced
 */
public record SaleOrderInvoicePreview(
        Integer saleOrderId,
        String jobNo,
        Integer customerId,
        String customerName,
        Integer jobTypeId,
        String jobTypeName,
        String employeeName,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate invoiceDate,
        String billType,
        Double currencyValue,
        Integer symbolRefId,
        String currentStatus,
        String completedStatus,
        List<Line> lines,
        Double subtotal,
        Double taxTotal,
        Double netTotal,
        SaleOrderInvoiceLink existing,
        boolean canCreate,
        List<String> blockers,
        List<String> warnings) {

    /** One invoice line, already calculated the way the invoice screen does it. */
    public record Line(
            Integer itemMasterRefId,
            String productCode,
            String productName,
            String remarks,
            String uom,
            Double quantity,
            Double rate,
            Double taxPercent,
            Double taxAmount,
            Double amount) {
    }
}
