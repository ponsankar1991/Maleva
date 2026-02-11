package my.maleva.api.integration.qne;

import lombok.RequiredArgsConstructor;
import my.maleva.api.config.QneProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class QneUrlBuilder {

    private final QneProperties qne;

    private String build(String... segments) {
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(qne.getBaseUrl());

        for (String segment : segments) {
            builder.pathSegment(segment);
        }

        return builder.toUriString();
    }

    // ===== CUSTOMER =====
    public String customerApi() {
        return build("Customers");
    }

    // ===== SUPPLIER =====
    public String supplierApi() {
        return build("Suppliers");
    }

    // ===== SALES =====
    public String salesInvoiceApi() {
        return build("SalesInvoices");
    }

    public String salesCNApi() {
        return build("SalesCNs");
    }

    public String salesCNKnockoffApi() {
        return build("SalesCNs", "Knockoff");
    }

    // ===== RECEIPTS =====
    public String customerReceiptApi() {
        return build("CustomerReceipts");
    }

    public String customerReceiptKnockoffApi() {
        return build("CustomerReceipts", "Knockoff");
    }

    public String customerReceiptMatchApi() {
        return build("CustomerReceipts", "Match");
    }

    // ===== BILLS =====
    public String billsApi() {
        return build("Bills");
    }

    public String payBillsApi() {
        return build("PayBills");
    }

    public String paymentVouchersApi() {
        return build("PaymentVouchers");
    }

    // ===== STOCKS =====
    public String stocksApi() {
        return build("Stocks");
    }

    // ===== REPORTS =====
    public String reportBase() {
        return build("Reports");
    }

    public String customerStatementApi() {
        return build("Reports", "CustomerStatement", "Url");
    }

    public String salesInvoiceReport(String id) {
        return build("Reports", "SalesInvoices", id, "Url");
    }

    public String receiptReport(String id) {
        return build("Reports", "Receipts", id, "Url");
    }

    public String salesCNReport(String id) {
        return build("Reports", "SalesCN", id, "Url");
    }
}
