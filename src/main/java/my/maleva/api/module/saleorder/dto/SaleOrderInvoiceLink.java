package my.maleva.api.module.saleorder.dto;

/**
 * Whether a sale order has already been billed, and if so which invoice
 * carries it — the answer the "Push Invoice" button needs before it sends
 * the operator to the invoice screen.
 *
 * <p>Legacy asked the same question through {@code SelectInvoiceNumber} but
 * could only answer yes or no, so the screen could say "Invoice Already
 * Exists for this Job" and nothing more. Naming the invoice lets the screen
 * offer to open it instead of leaving the operator to go and find it.
 */
public record SaleOrderInvoiceLink(
        /** The sale order this describes. */
        Integer saleOrderId,
        String jobNo,
        String customerName,
        /** True when an active invoice already bills this job. */
        boolean invoiced,
        Integer invoiceId,
        String invoiceNo,
        /** dd/MM/yyyy, as the screens show dates. */
        String invoiceDate,
        /** Empty until the invoice has been pushed to QNE. */
        String qneCode,
        /** Empty until the invoice has been submitted to LHDN. */
        String eInvoiceUid) {

    public static SaleOrderInvoiceLink notInvoiced(Integer saleOrderId, String jobNo, String customerName) {
        return new SaleOrderInvoiceLink(saleOrderId, jobNo, customerName, false, null, "", "", "", "");
    }
}
