package my.maleva.api.module.paymentrecept.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.QneProperties;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QneKnockoffItem;
import my.maleva.api.integration.qne.dto.QneKnockoffRequest;
import my.maleva.api.integration.qne.dto.QneReceiptRequest;
import my.maleva.api.integration.qne.dto.QneReceiptResponse;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.customer.repository.CustomerRepository;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import my.maleva.api.module.master.repository.BankMasterRepository;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import my.maleva.api.module.paymentrecept.entity.ReceiptDetails;
import my.maleva.api.module.paymentrecept.repository.ReceiptDetailsRepository;
import my.maleva.api.module.paymentrecept.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * QNE push for customer receipts — the Java port of the QNE side of legacy
 * {@code ReceiptServices.ReceiptVIEW}: create the receipt, persist its ids,
 * then knock it off against the invoices its detail rows reference
 * ({@code KnockoffRefId} = the invoice's QNE id).
 *
 * <p>Legacy checked the wrong response after the knockoff POST (the ro1/ro2
 * bug), so knockoff failures vanished. Here the knockoff outcome is real: a
 * failed knockoff reports {@code IsSuccess=false} while keeping the receipt's
 * ids (the receipt does exist in QNE at that point).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptQneService {

    private final QneGateway gateway;
    private final QneProperties properties;
    private final ReceiptRepository receipts;
    private final ReceiptDetailsRepository receiptDetails;
    private final CustomerRepository customers;
    private final BankMasterRepository banks;
    private final SaleMasterRepository saleMasters;

    public QnePushResult push(Integer receiptId, Integer companyId) {
        Receipt receipt = receipts.findById(receiptId).orElse(null);
        if (receipt == null) {
            return QnePushResult.localError(404, "Receipt not found: " + receiptId);
        }
        if (!Objects.equals(receipt.getCompanyRefId(), companyId)) {
            return QnePushResult.localError(403, "Receipt does not belong to company " + companyId);
        }
        if (!QnePayloads.isBlank(receipt.getQneCode())) {
            return QnePushResult.alreadyPushed(receipt.getQneId(), receipt.getQneCode(),
                    "Receipt already exists in QNE as " + receipt.getQneCode());
        }
        Customer customer = customers.findById(receipt.getCustomerRefId()).orElse(null);
        if (customer == null) {
            return QnePushResult.localError(409, "Customer not found for receipt " + receiptId);
        }
        if (QnePayloads.isBlank(customer.getCompanyCode())) {
            return QnePushResult.localError(409,
                    "Customer '" + customer.getCustomerName() + "' is not in QNE yet — push the customer first");
        }
        String depositAccountCode = receipt.getBankRefId() == null ? "" :
                banks.findById(receipt.getBankRefId())
                        .map(bank -> QnePayloads.orEmpty(bank.getQneCode()))
                        .orElse("");
        if (QnePayloads.isBlank(depositAccountCode)) {
            return QnePushResult.localError(409,
                    "The receipt's bank account has no QNE code mapped on BankMaster — seed it first");
        }

        QneCall<QneReceiptResponse> call = gateway.createCustomerReceipt(
                buildRequest(receipt, customer, depositAccountCode));
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }
        QneReceiptResponse created = call.data();
        receipts.claimQneIdentity(receipt.getId(), created.getId(), created.getDocCode());

        List<QneKnockoffItem> items = buildKnockoffItems(receipt);
        if (!items.isEmpty()) {
            QneCall<String> knockoff = gateway.knockoffCustomerReceipt(QneKnockoffRequest.builder()
                    .docId(created.getId())
                    .knockoffItems(items)
                    .build());
            if (!knockoff.success()) {
                return new QnePushResult(false, created.getId(), created.getDocCode(),
                        reportUrl(created.getId()),
                        "Receipt created in QNE as " + created.getDocCode()
                                + " but the invoice knockoff failed: " + knockoff.message(),
                        null);
            }
        }

        return QnePushResult.ok(created.getId(), created.getDocCode(), reportUrl(created.getId()),
                "Receipt pushed to QNE as " + created.getDocCode());
    }

    /**
     * One knockoff item per detail row that references a sale invoice —
     * customer-open rows have no QNE counterpart, matching the legacy inner
     * join. An invoice never pushed to QNE contributes an empty
     * KnockoffRefId, exactly as legacy sent it (QNE rejects it with its own
     * message).
     */
    private List<QneKnockoffItem> buildKnockoffItems(Receipt receipt) {
        List<ReceiptDetails> rows = receiptDetails.findByReceiptRefId(receipt.getId());
        Map<Integer, SaleMaster> invoices = saleMasters.findAllById(
                        rows.stream().map(ReceiptDetails::getSaleMasterRefId).filter(Objects::nonNull).distinct().toList())
                .stream().collect(Collectors.toMap(SaleMaster::getId, Function.identity()));

        List<QneKnockoffItem> items = new ArrayList<>();
        for (ReceiptDetails row : rows) {
            SaleMaster invoice = row.getSaleMasterRefId() == null
                    ? null : invoices.get(row.getSaleMasterRefId());
            if (invoice == null) {
                continue;
            }
            items.add(QneKnockoffItem.builder()
                    .payment(QnePayloads.d(row.getReceiptAmount()))
                    .knockoffRefId(QnePayloads.orEmpty(invoice.getQneId()))
                    .build());
        }
        return items;
    }

    /** Header mapping pinned by legacy {@code ReceiptVIEW}'s flattened select. */
    static QneReceiptRequest buildRequest(Receipt receipt, Customer customer, String depositAccountCode) {
        return QneReceiptRequest.builder()
                .customerCode(customer.getCompanyCode())
                .docDate(QnePayloads.date(receipt.getReceiptDate()))
                .amount(QnePayloads.d(receipt.getAmount()))
                .bankCharges(QnePayloads.d(receipt.getBankCharges()))
                .depositAccountCode(depositAccountCode)
                .currencyRate(QnePayloads.d(receipt.getCurrencyValue()))
                .build();
    }

    private String reportUrl(String qneId) {
        if (!properties.isView() || QnePayloads.isBlank(qneId)) {
            return null;
        }
        QneCall<String> call = gateway.receiptReportUrl(qneId);
        if (!call.success()) {
            log.warn("QNE receipt report URL fetch failed for {}: {}", qneId, call.message());
            return null;
        }
        return call.data();
    }
}
