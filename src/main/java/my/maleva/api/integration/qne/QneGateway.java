package my.maleva.api.integration.qne;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.integration.qne.dto.QneBillRequest;
import my.maleva.api.integration.qne.dto.QneBillResponse;
import my.maleva.api.integration.qne.dto.QneCustomerRequest;
import my.maleva.api.integration.qne.dto.QneCustomerResponse;
import my.maleva.api.integration.qne.dto.QneKnockoffRequest;
import my.maleva.api.integration.qne.dto.QnePayBillRequest;
import my.maleva.api.integration.qne.dto.QnePaymentResponse;
import my.maleva.api.integration.qne.dto.QnePaymentVoucherRequest;
import my.maleva.api.integration.qne.dto.QneReceiptRequest;
import my.maleva.api.integration.qne.dto.QneReceiptResponse;
import my.maleva.api.integration.qne.dto.QneReportUrlResponse;
import my.maleva.api.integration.qne.dto.QneSalesCnRequest;
import my.maleva.api.integration.qne.dto.QneSalesCnResponse;
import my.maleva.api.integration.qne.dto.QneSalesInvoiceRequest;
import my.maleva.api.integration.qne.dto.QneSalesInvoiceResponse;
import my.maleva.api.integration.qne.dto.QneStockRequest;
import my.maleva.api.integration.qne.dto.QneStockResponse;
import my.maleva.api.integration.qne.dto.QneSupplierRequest;
import my.maleva.api.integration.qne.dto.QneSupplierResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Typed operations against QNE — every operation the legacy system performed,
 * one method each, with transport and parsing handled here so module services
 * only build payloads and persist the returned ids.
 *
 * <p>The operation set mirrors the legacy call sites exactly (see
 * {@code docs/QNE_OPERATIONS_CATALOG.md}): documents are <b>create-once</b> —
 * each legacy flow fired only while the local row's QNECode was empty, and
 * every update path except the sales-invoice PUT was dead code. Callers own
 * that empty-code guard and the write-back of {@code Id}/code to their table,
 * because both live with the entity.
 *
 * <p>Ordering rules callers must respect (QNE rejects otherwise):
 * customers before invoices/CNs/receipts, suppliers before bills/pay-bills,
 * stocks before invoice and CN lines, and an invoice before any knockoff that
 * references it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QneGateway {

    private final QneClient client;
    private final QneUrlBuilder urls;
    private final ObjectMapper objectMapper;

    /* ── Customers ─────────────────────────────────────────────────── */

    /**
     * Create a customer. The response carries QNE's id and CompanyCode,
     * which legacy persisted to {@code Customer.UpdateId} / {@code CompanyCode}.
     */
    public QneCall<QneCustomerResponse> createCustomer(QneCustomerRequest request) {
        return parse(client.post(urls.customerApi(), request), QneCustomerResponse.class);
    }

    /** Look up customers by their QNE company codes, for id backfill. */
    public QneCall<List<QneCustomerResponse>> findCustomersByCompanyCodes(List<String> codes) {
        String url = urls.customerApi() + inFilter("companyCode", codes);
        return parseList(client.get(url), QneCustomerResponse.class);
    }

    /** Pull the QNE customer list, for the reconcile/pull sync. */
    public QneCall<List<QneCustomerResponse>> listCustomers(int top) {
        return parseList(client.get(urls.customerApi() + "?top=" + top), QneCustomerResponse.class);
    }

    /* ── Suppliers ─────────────────────────────────────────────────── */

    /** Create a supplier; ids persist to {@code Supplier.QNEId} / {@code QNECode}. */
    public QneCall<QneSupplierResponse> createSupplier(QneSupplierRequest request) {
        return parse(client.post(urls.supplierApi(), request), QneSupplierResponse.class);
    }

    public QneCall<List<QneSupplierResponse>> findSuppliersByCompanyCodes(List<String> codes) {
        String url = urls.supplierApi() + inFilter("companyCode", codes);
        return parseList(client.get(url), QneSupplierResponse.class);
    }

    public QneCall<List<QneSupplierResponse>> listSuppliers(int top) {
        return parseList(client.get(urls.supplierApi() + "?top=" + top), QneSupplierResponse.class);
    }

    /* ── Stocks ────────────────────────────────────────────────────── */

    /** Create a stock item; ids persist to {@code ItemMaster.QNEId} / {@code QNECode}. */
    public QneCall<QneStockResponse> createStock(QneStockRequest request) {
        return parse(client.post(urls.stocksApi(), request), QneStockResponse.class);
    }

    public QneCall<List<QneStockResponse>> findStocksByStockCodes(List<String> codes) {
        String url = urls.stocksApi() + inFilter("stockCode", codes);
        return parseList(client.get(url), QneStockResponse.class);
    }

    /* ── Sales invoices ────────────────────────────────────────────── */

    /** Create a sales invoice; ids persist to {@code SaleMaster.QNEId} / {@code QNECode}. */
    public QneCall<QneSalesInvoiceResponse> createSalesInvoice(QneSalesInvoiceRequest request) {
        return parse(client.post(urls.salesInvoiceApi(), request), QneSalesInvoiceResponse.class);
    }

    /**
     * Update a sales invoice — the one live PUT the legacy system had.
     * QNE addresses the document by the {@code Id} inside the body, not the
     * URL, which is why this goes to the bare endpoint.
     */
    public QneCall<QneSalesInvoiceResponse> updateSalesInvoice(QneSalesInvoiceRequest request) {
        return parse(client.put(urls.salesInvoiceApi(), request), QneSalesInvoiceResponse.class);
    }

    /* ── Sales credit notes ────────────────────────────────────────── */

    /** Create a credit note; ids persist to {@code SaleCreditMaster.QNEId} / {@code QNECode}. */
    public QneCall<QneSalesCnResponse> createSalesCn(QneSalesCnRequest request) {
        return parse(client.post(urls.salesCNApi(), request), QneSalesCnResponse.class);
    }

    /* ── Customer receipts ─────────────────────────────────────────── */

    /** Create a receipt; ids persist to {@code Receipt.QNEId} / {@code QNECode}. */
    public QneCall<QneReceiptResponse> createCustomerReceipt(QneReceiptRequest request) {
        return parse(client.post(urls.customerReceiptApi(), request), QneReceiptResponse.class);
    }

    /**
     * Knock a receipt off against invoices. {@code DocId} is the id returned
     * by {@link #createCustomerReceipt}; each item's {@code KnockoffRefId} is
     * the invoice's QNE id.
     *
     * <p>The legacy code sent this and then checked the <em>insert</em>
     * result instead of the knockoff result, so knockoff failures vanished.
     * Callers here get the real outcome — check it.
     */
    public QneCall<String> knockoffCustomerReceipt(QneKnockoffRequest request) {
        return raw(client.post(urls.customerReceiptKnockoffApi(), request));
    }

    /* ── Bills / payments ──────────────────────────────────────────── */

    /** Create a supplier bill; ids persist to {@code BillMaster.QNEId} / {@code QNECode}. */
    public QneCall<QneBillResponse> createBill(QneBillRequest request) {
        return parse(client.post(urls.billsApi(), request), QneBillResponse.class);
    }

    /** Pay a supplier bill; ids persist to {@code Payment.QNEId} / {@code QNECode}. */
    public QneCall<QnePaymentResponse> createPayBill(QnePayBillRequest request) {
        return parse(client.post(urls.payBillsApi(), request), QnePaymentResponse.class);
    }

    /** Create a payment voucher; ids persist to {@code PaymentVoucherMaster.QNEId} / {@code QNECode}. */
    public QneCall<QnePaymentResponse> createPaymentVoucher(QnePaymentVoucherRequest request) {
        return parse(client.post(urls.paymentVouchersApi(), request), QnePaymentResponse.class);
    }

    /* ── QNE-hosted report documents ───────────────────────────────── */

    /** URL of the QNE-rendered invoice document, by {@code SaleMaster.QNEId}. */
    public QneCall<String> salesInvoiceReportUrl(String qneId) {
        return reportUrl(urls.salesInvoiceReport(qneId));
    }

    /** URL of the QNE-rendered receipt document, by {@code Receipt.QNEId}. */
    public QneCall<String> receiptReportUrl(String qneId) {
        return reportUrl(urls.receiptReport(qneId));
    }

    /** URL of the QNE-rendered credit note, by {@code SaleCreditMaster.QNEId}. */
    public QneCall<String> salesCnReportUrl(String qneId) {
        return reportUrl(urls.salesCNReport(qneId));
    }

    /** URL of the customer statement, by the customer's QNE id ({@code Customer.UpdateId}). */
    public QneCall<String> customerStatementUrl(String customerQneId, int year, int month) {
        return reportUrl(urls.customerStatementUrl(customerQneId, year, month));
    }

    /* ── plumbing ──────────────────────────────────────────────────── */

    /**
     * QNE's non-standard membership filter, byte-for-byte the legacy shape:
     * {@code ?$filter=field in ['a','b']}. Built by hand rather than through
     * an encoding URI builder because QNE accepted the legacy's raw brackets,
     * quotes and spaces, and fully percent-encoding them is the untested
     * variant. Only {@code &} is escaped, exactly as legacy did — a literal
     * ampersand in a code would otherwise split the query.
     */
    static String inFilter(String field, List<String> values) {
        String joined = values.stream()
                .map(v -> "'" + (v == null ? "" : v.replace("&", "%26")) + "'")
                .collect(Collectors.joining(","));
        return "?$filter=" + field + " in [" + joined + "]";
    }

    private <T> QneCall<T> parse(QneResult result, Class<T> type) {
        if (!result.success()) {
            return new QneCall<>(result, null);
        }
        try {
            return new QneCall<>(result, objectMapper.readValue(result.body(), type));
        } catch (Exception ex) {
            log.error("QNE response did not parse as {}: {}", type.getSimpleName(), result.body(), ex);
            return new QneCall<>(
                    QneResult.failed(result.status(), result.body(),
                            "QNE response could not be parsed: " + ex.getMessage()),
                    null);
        }
    }

    /**
     * Collection responses arrive as a bare JSON array — that is what the
     * legacy deserialised — but an OData {@code {"value": [...]}} envelope is
     * unwrapped too, so a QNE-side upgrade does not silently break the syncs.
     */
    private <T> QneCall<List<T>> parseList(QneResult result, Class<T> type) {
        if (!result.success()) {
            return new QneCall<>(result, null);
        }
        try {
            JsonNode node = objectMapper.readTree(result.body());
            if (node.isObject() && node.has("value")) {
                node = node.get("value");
            }
            List<T> items = objectMapper.readerForListOf(type).readValue(node);
            return new QneCall<>(result, items);
        } catch (Exception ex) {
            log.error("QNE list response did not parse as {}: {}", type.getSimpleName(), result.body(), ex);
            return new QneCall<>(
                    QneResult.failed(result.status(), result.body(),
                            "QNE response could not be parsed: " + ex.getMessage()),
                    null);
        }
    }

    /** For operations whose body is not read beyond success/failure. */
    private QneCall<String> raw(QneResult result) {
        return new QneCall<>(result, result.success() ? result.body() : null);
    }

    private QneCall<String> reportUrl(String url) {
        QneCall<QneReportUrlResponse> call = parse(client.get(url), QneReportUrlResponse.class);
        return new QneCall<>(call.result(), call.data() == null ? null : call.data().getFile());
    }
}
