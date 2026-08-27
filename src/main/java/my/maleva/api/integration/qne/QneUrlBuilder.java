package my.maleva.api.integration.qne;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.config.QneProperties;
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

    /**
     * Appends a QNE OData-style query to an endpoint URL.
     *
     * <p>Values are URL-encoded here rather than concatenated raw the way the
     * legacy call sites did it — a filter on a name containing {@code &} or a
     * space survives encoding, and QNE accepts standard percent-encoding.
     */
    public String withQuery(String endpointUrl, QneODataQuery query) {
        if (query == null || query.isEmpty()) {
            return endpointUrl;
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endpointUrl);
        query.params().forEach(builder::queryParam);
        return builder.encode().toUriString();
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

    /**
     * Customer statement for one month, addressed by the customer's QNE id
     * (stored locally as {@code Customer.UpdateId} — the misleading column
     * name is legacy's).
     */
    public String customerStatementUrl(String customerQneId, int year, int month) {
        return UriComponentsBuilder.fromUriString(customerStatementApi())
                .queryParam("customerId", customerQneId)
                .queryParam("year", year)
                .queryParam("month", month)
                .toUriString();
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
