package my.maleva.api.module.invoice.view;

import java.util.List;

/** The view's two grids: invoice headers and every line of those invoices. */
public record SaleInvoiceViewResult(List<SaleInvoiceViewRow> master, List<SaleInvoiceViewDetailRow> details) {
}
