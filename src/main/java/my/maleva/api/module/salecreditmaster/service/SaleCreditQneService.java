package my.maleva.api.module.salecreditmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.QneProperties;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QneSalesCnLine;
import my.maleva.api.integration.qne.dto.QneSalesCnRequest;
import my.maleva.api.integration.qne.dto.QneSalesCnResponse;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.itemmaster.repository.ItemMasterRepository;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditDetails;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditDetailsRepository;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditMasterRepository;
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
 * QNE push for sales credit notes — the Java port of the QNE side of legacy
 * {@code SaleCreditServices.SaleCreditVIEW}. Legacy fired this as a side
 * effect of viewing/printing the CN; here it is an explicit push, still
 * guarded by the empty QNECode. The CN's ReferenceNo carries the original
 * invoice's QNE code (empty when that invoice was never pushed — legacy
 * isnull()-ed the join).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleCreditQneService {

    private final QneGateway gateway;
    private final QneProperties properties;
    private final SaleCreditMasterRepository creditMasters;
    private final SaleCreditDetailsRepository creditDetails;
    private final SaleMasterRepository saleMasters;
    private final CustomerRepository customers;
    private final PaymentTermsMasterRepository paymentTerms;
    private final ItemMasterRepository itemMasters;
    private final UomRepository uoms;

    public QnePushResult push(Integer creditNoteId, Integer companyId) {
        SaleCreditMaster creditNote = creditMasters.findById(creditNoteId).orElse(null);
        if (creditNote == null) {
            return QnePushResult.localError(404, "Credit note not found: " + creditNoteId);
        }
        if (!Objects.equals(creditNote.getCompanyRefId(), companyId)) {
            return QnePushResult.localError(403, "Credit note does not belong to company " + companyId);
        }
        if (!QnePayloads.isBlank(creditNote.getQneCode())) {
            return QnePushResult.alreadyPushed(creditNote.getQneId(), creditNote.getQneCode(),
                    "Credit note already exists in QNE as " + creditNote.getQneCode());
        }
        Customer customer = customers.findById(creditNote.getCustomerRefId()).orElse(null);
        if (customer == null) {
            return QnePushResult.localError(409, "Customer not found for credit note " + creditNoteId);
        }
        if (QnePayloads.isBlank(customer.getCompanyCode())) {
            return QnePushResult.localError(409,
                    "Customer '" + customer.getCustomerName() + "' is not in QNE yet — push the customer first");
        }

        String invoiceQneCode = creditNote.getSaleMasterRefId() == null ? "" :
                saleMasters.findById(creditNote.getSaleMasterRefId())
                        .map(sm -> QnePayloads.orEmpty(sm.getQneCode()))
                        .orElse("");

        QneSalesCnRequest request = buildRequest(
                creditNote, customer, termName(customer), invoiceQneCode, buildLines(creditNote));
        QneCall<QneSalesCnResponse> call = gateway.createSalesCn(request);
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }

        creditMasters.claimQneIdentity(creditNote.getId(), call.data().getId(), call.data().getCnCode());
        return QnePushResult.ok(call.data().getId(), call.data().getCnCode(),
                reportUrl(call.data().getId()),
                "Credit note pushed to QNE as " + call.data().getCnCode());
    }

    private String termName(Customer customer) {
        if (customer.getPaymentTermsRefid() == null) {
            return "";
        }
        return paymentTerms.findById(customer.getPaymentTermsRefid())
                .map(PaymentTermsMaster::getTermsName)
                .orElse("");
    }

    private List<QneSalesCnLine> buildLines(SaleCreditMaster creditNote) {
        List<SaleCreditDetails> details = creditDetails.findBySaleCreditMasterRefId(creditNote.getId());
        Map<Integer, ItemMaster> items = itemMasters.findAllById(
                        details.stream().map(SaleCreditDetails::getItemMasterRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(ItemMaster::getId, Function.identity()));
        Map<Integer, String> uomNames = uoms.findAllById(
                        items.values().stream().map(ItemMaster::getUomCode).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(Uom::getId, u -> QnePayloads.orEmpty(u.getDescription())));

        String cnDate = QnePayloads.date(creditNote.getSaleDate());
        List<QneSalesCnLine> lines = new ArrayList<>();
        for (SaleCreditDetails detail : details) {
            ItemMaster item = items.get(detail.getItemMasterRefId());
            if (item == null) {
                log.warn("Credit note {} line {} references missing ItemMaster {} — line skipped",
                        creditNote.getId(), detail.getId(), detail.getItemMasterRefId());
                continue;
            }
            lines.add(buildLine(detail, item,
                    item.getUomCode() == null ? "" : uomNames.getOrDefault(item.getUomCode(), ""),
                    cnDate));
        }
        return lines;
    }

    /** CN lines describe by product name only — no remarks override, unlike invoices. */
    static QneSalesCnLine buildLine(SaleCreditDetails detail, ItemMaster item,
                                    String uomDescription, String cnDate) {
        return QneSalesCnLine.builder()
                .stock(QnePayloads.htmlEncode(item.getProdCode()))
                .description(item.getPName())
                .qty(QnePayloads.d(detail.getItemQty()))
                .uom(uomDescription)
                .unitPrice(QnePayloads.d(detail.getSalesRate()))
                .dateRef1(cnDate)
                .dateRef2(cnDate)
                .build();
    }

    /** Header mapping pinned by legacy {@code SaleCreditVIEW} — including its hardcoded true/true tax flags. */
    static QneSalesCnRequest buildRequest(SaleCreditMaster creditNote, Customer customer, String term,
                                          String invoiceQneCode, List<QneSalesCnLine> lines) {
        return QneSalesCnRequest.builder()
                .customer(customer.getCompanyCode())
                .cnDate(QnePayloads.date(creditNote.getSaleDate()))
                .term(term)
                .referenceNo(invoiceQneCode)
                .attention(customer.getCity())
                .currencyRate(QnePayloads.d(creditNote.getCurrencyValue()))
                .isTaxInclusive(true)
                .isRounding(true)
                .details(lines)
                .build();
    }

    private String reportUrl(String qneId) {
        if (!properties.isView() || QnePayloads.isBlank(qneId)) {
            return null;
        }
        QneCall<String> call = gateway.salesCnReportUrl(qneId);
        if (!call.success()) {
            log.warn("QNE credit note report URL fetch failed for {}: {}", qneId, call.message());
            return null;
        }
        return call.data();
    }
}
