package my.maleva.api.module.invoice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.QneProperties;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QneSalesInvoiceLine;
import my.maleva.api.integration.qne.dto.QneSalesInvoiceRequest;
import my.maleva.api.integration.qne.dto.QneSalesInvoiceResponse;
import my.maleva.api.module.billing.bill.entity.DoMaster;
import my.maleva.api.module.billing.bill.repository.DoMasterRepository;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.invoice.entity.SaleDetails;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleDetailsRepository;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.invoice.repository.SaleMasterReferenceRepository;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.repository.UomRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * QNE push for sales invoices — the Java port of legacy
 * {@code SaleInvoiceServices.InvoiceConvert} (create, while QNECode is empty)
 * and {@code InvoiceConvertEdit} (the one live PUT in the legacy codebase,
 * once QNECode is set). Ordering contract: the customer and every line's
 * stock must already exist in QNE.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleInvoiceQneService {

    private final QneGateway gateway;
    private final QneProperties properties;
    private final SaleMasterRepository saleMasters;
    private final SaleDetailsRepository saleDetails;
    private final SaleMasterReferenceRepository saleMasterReferences;
    private final CustomerRepository customers;
    private final PaymentTermsMasterRepository paymentTerms;
    private final ItemMasterRepository itemMasters;
    private final UomRepository uoms;
    private final DoMasterRepository doMasters;

    public QnePushResult push(Integer invoiceId, Integer companyId) {
        SaleMaster invoice = saleMasters.findById(invoiceId).orElse(null);
        if (invoice == null) {
            return QnePushResult.localError(404, "Invoice not found: " + invoiceId);
        }
        if (!Objects.equals(invoice.getCompanyRefId(), companyId)) {
            return QnePushResult.localError(403, "Invoice does not belong to company " + companyId);
        }
        Customer customer = customers.findById(invoice.getCustomerRefId()).orElse(null);
        if (customer == null) {
            return QnePushResult.localError(409, "Customer not found for invoice " + invoiceId);
        }
        if (QnePayloads.isBlank(customer.getCompanyCode())) {
            return QnePushResult.localError(409,
                    "Customer '" + customer.getCustomerName() + "' is not in QNE yet — push the customer first");
        }

        boolean multiReference = saleMasterReferences.countBySaleMasterRefId(invoice.getId()) > 1;
        QneSalesInvoiceRequest request = buildRequest(
                invoice, customer, termName(customer), multiReference,
                doNumberDisplay(invoice), buildLines(invoice));

        if (!QnePayloads.isBlank(invoice.getQneCode())) {
            // Legacy InvoiceConvertEdit: a live PUT to the bare endpoint with
            // the QNE id only in the body; the id columns never change.
            request.setId(invoice.getQneId());
            QneCall<QneSalesInvoiceResponse> call = gateway.updateSalesInvoice(request);
            if (!call.success()) {
                return QnePushResult.rejected(call.message());
            }
            return QnePushResult.ok(invoice.getQneId(), invoice.getQneCode(),
                    reportUrl(invoice.getQneId()),
                    "Invoice updated in QNE (" + invoice.getQneCode() + ")");
        }

        QneCall<QneSalesInvoiceResponse> call = gateway.createSalesInvoice(request);
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }
        saleMasters.claimQneIdentity(invoice.getId(), call.data().getId(), call.data().getInvoiceCode());
        return QnePushResult.ok(call.data().getId(), call.data().getInvoiceCode(),
                reportUrl(call.data().getId()),
                "Invoice pushed to QNE as " + call.data().getInvoiceCode());
    }

    private String termName(Customer customer) {
        if (customer.getPaymentTermsRefid() == null) {
            return "";
        }
        return paymentTerms.findById(customer.getPaymentTermsRefid())
                .map(PaymentTermsMaster::getTermsName)
                .orElse("");
    }

    private String doNumberDisplay(SaleMaster invoice) {
        if (invoice.getDocNo() == null) {
            return null;
        }
        // Legacy LEFT JOIN with no isnull() — a missing DoMaster row sends null.
        return doMasters.findById(invoice.getDocNo())
                .map(DoMaster::getCNumberDisplay)
                .orElse(null);
    }

    private List<QneSalesInvoiceLine> buildLines(SaleMaster invoice) {
        List<SaleDetails> details = saleDetails.findBySaleMasterRefId(invoice.getId());
        Map<Integer, ItemMaster> items = itemMasters.findAllById(
                        details.stream().map(SaleDetails::getItemMasterRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(ItemMaster::getId, Function.identity()));
        Map<Integer, String> uomNames = uoms.findAllById(
                        items.values().stream().map(ItemMaster::getUomCode).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Uom::getId, u -> QnePayloads.orEmpty(u.getDescription())));

        String invoiceDate = QnePayloads.date(invoice.getSaleDate());
        List<QneSalesInvoiceLine> lines = new ArrayList<>();
        for (SaleDetails detail : details) {
            ItemMaster item = items.get(detail.getItemMasterRefId());
            if (item == null) {
                // Legacy inner-joined ItemMaster, silently dropping such rows.
                log.warn("Invoice {} line {} references missing ItemMaster {} — line skipped",
                        invoice.getId(), detail.getId(), detail.getItemMasterRefId());
                continue;
            }
            lines.add(buildLine(detail, item,
                    item.getUomCode() == null ? "" : uomNames.getOrDefault(item.getUomCode(), ""),
                    invoiceDate));
        }
        return lines;
    }

    static QneSalesInvoiceLine buildLine(SaleDetails detail, ItemMaster item,
                                         String uomDescription, String invoiceDate) {
        return QneSalesInvoiceLine.builder()
                .stock(QnePayloads.htmlEncode(item.getProdCode()))
                .description(!QnePayloads.isBlank(detail.getSdRemarks())
                        ? detail.getSdRemarks() : item.getPName())
                .qty(QnePayloads.d(detail.getItemQty()))
                .uom(uomDescription)
                .unitPrice(QnePayloads.d(detail.getSalesRate()))
                .dateRef1(invoiceDate)
                .dateRef2(invoiceDate)
                .build();
    }

    /**
     * Header mapping pinned by legacy {@code InvoiceConvert}. An invoice
     * covering more than one SaleMasterReference row blanks Ref1–Ref5,
     * Remark1–3 and Title — Attention and ReferenceNo stay populated in both
     * branches. IsTaxInclusive/IsRounding are false: that is what the
     * SaleInvoice screen sent; the legacy SaleOrder conversion sent true/true
     * to the same endpoint (catalog ambiguity #1) and loses that behaviour
     * deliberately in this single Java call site.
     */
    static QneSalesInvoiceRequest buildRequest(SaleMaster invoice, Customer customer, String term,
                                               boolean multiReference, String doNumberDisplay,
                                               List<QneSalesInvoiceLine> lines) {
        QneSalesInvoiceRequest.QneSalesInvoiceRequestBuilder builder = QneSalesInvoiceRequest.builder()
                .customer(customer.getCompanyCode())
                .invoiceDate(QnePayloads.date(invoice.getSaleDate()))
                .term(term)
                .attention(customer.getCity())
                .referenceNo(invoice.getRemarks1())
                .currencyRate(QnePayloads.d(invoice.getCurrencyValue()))
                .isTaxInclusive(false)
                .isRounding(false)
                .details(lines);

        if (multiReference) {
            builder.ref1("").ref2("").ref3("").ref4("").ref5("")
                    .remark1("").remark2("").remark3("")
                    .title("");
        } else {
            builder.ref1(invoice.getOrigin())
                    .ref2(invoice.getDestination())
                    .ref3(invoice.getQuantity())
                    .ref4(invoice.getTotalWeight())
                    .ref5(invoice.getOffvesselname())
                    .remark1(invoice.getLoadingvesselname())
                    .remark2(doNumberDisplay)
                    .remark3(invoice.getCommodity())
                    // Legacy CASE was degenerate: with an empty loading vessel
                    // the title is empty even when the off vessel is named.
                    .title(QnePayloads.isBlank(invoice.getLoadingvesselname())
                            && QnePayloads.isBlank(invoice.getOffvesselname())
                            ? "SALES" : invoice.getLoadingvesselname());
        }
        return builder.build();
    }

    private String reportUrl(String qneId) {
        if (!properties.isView() || QnePayloads.isBlank(qneId)) {
            return null;
        }
        QneCall<String> call = gateway.salesInvoiceReportUrl(qneId);
        if (!call.success()) {
            log.warn("QNE invoice report URL fetch failed for {}: {}", qneId, call.message());
            return null;
        }
        return call.data();
    }
}
