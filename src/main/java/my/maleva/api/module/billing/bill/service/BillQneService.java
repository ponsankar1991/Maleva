package my.maleva.api.module.billing.bill.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QneBillLine;
import my.maleva.api.integration.qne.dto.QneBillRequest;
import my.maleva.api.integration.qne.dto.QneBillResponse;
import my.maleva.api.module.accounting.entity.GLAccounts;
import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import my.maleva.api.module.billing.bill.entity.BillDetails;
import my.maleva.api.module.billing.bill.entity.BillMaster;
import my.maleva.api.module.billing.bill.repository.BillDetailsRepository;
import my.maleva.api.module.billing.bill.repository.BillMasterRepository;
import my.maleva.api.module.master.entity.PaymentTermsMaster;
import my.maleva.api.module.master.entity.SymbolMaster;
import my.maleva.api.module.master.repository.SymbolMasterRepository;
import my.maleva.api.module.payment.repository.PaymentTermsMasterRepository;
import my.maleva.api.module.supplier.entity.Supplier;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * QNE push for supplier bills — the Java port of the QNE side of legacy
 * {@code BillMasterServices.BillMasterConvert}. Term and currency resolve
 * through the supplier (BillMaster has no symbol column), and the detail
 * accounts through {@code GLAccounts.RowIndex = BillDetails.AccountMasterRefId}.
 *
 * <p>Legacy's INNER join silently dropped any line whose account ref had no
 * GLAccounts row, posting the bill to QNE short of lines. Here that is a
 * refusal with the offending refs named — retry after mapping the account.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillQneService {

    private final QneGateway gateway;
    private final BillMasterRepository bills;
    private final BillDetailsRepository billDetails;
    private final SupplierRepository suppliers;
    private final SymbolMasterRepository symbols;
    private final PaymentTermsMasterRepository paymentTerms;
    private final GLAccountsRepository glAccounts;

    public QnePushResult push(Integer billId, Integer companyId) {
        BillMaster bill = bills.findById(billId).orElse(null);
        if (bill == null) {
            return QnePushResult.localError(404, "Bill not found: " + billId);
        }
        if (!Objects.equals(bill.getCompanyRefId(), companyId)) {
            return QnePushResult.localError(403, "Bill does not belong to company " + companyId);
        }
        if (bill.getActive() == null || bill.getActive() != 1) {
            return QnePushResult.localError(409, "Bill " + billId + " is not active");
        }
        if (!QnePayloads.isBlank(bill.getQneCode())) {
            return QnePushResult.alreadyPushed(bill.getQneId(), bill.getQneCode(),
                    "Bill already exists in QNE as " + bill.getQneCode());
        }
        Supplier supplier = suppliers.findById(bill.getSupplierRefId()).orElse(null);
        if (supplier == null) {
            return QnePushResult.localError(409, "Supplier not found for bill " + billId);
        }
        if (QnePayloads.isBlank(supplier.getQneCode())) {
            return QnePushResult.localError(409,
                    "Supplier '" + supplier.getSupplierName() + "' is not in QNE yet — push the supplier first");
        }

        List<BillDetails> details = billDetails.findByBillMasterRefId(bill.getId());
        Map<Integer, String> accounts = accountCodesByRowIndex(
                details.stream().map(BillDetails::getAccountMasterRefId).filter(Objects::nonNull).distinct().toList());
        List<Integer> unmapped = details.stream()
                .map(BillDetails::getAccountMasterRefId)
                .filter(ref -> ref == null || !accounts.containsKey(ref))
                .distinct()
                .toList();
        if (!unmapped.isEmpty()) {
            return QnePushResult.localError(409,
                    "No GL account is mapped for account refs " + unmapped
                            + " — the bill would post to QNE incomplete");
        }

        List<QneBillLine> lines = new ArrayList<>();
        for (BillDetails detail : details) {
            lines.add(QneBillLine.builder()
                    .account(accounts.get(detail.getAccountMasterRefId()))
                    .description(detail.getRemarksD())
                    .amount(QnePayloads.d(detail.getAmount()))
                    .build());
        }

        QneBillRequest request = buildRequest(bill, supplier,
                termName(supplier), currencyName(supplier), lines);
        QneCall<QneBillResponse> call = gateway.createBill(request);
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }

        bills.claimQneIdentity(bill.getId(), call.data().getId(), call.data().getBillCode());
        return QnePushResult.ok(call.data().getId(), call.data().getBillCode(), null,
                "Bill pushed to QNE as " + call.data().getBillCode());
    }

    private Map<Integer, String> accountCodesByRowIndex(List<Integer> rowIndexes) {
        Map<Integer, String> codes = new HashMap<>();
        if (rowIndexes.isEmpty()) {
            return codes;
        }
        for (GLAccounts account : glAccounts.findByRowIndexIn(rowIndexes)) {
            if (account.getRowIndex() != null && !QnePayloads.isBlank(account.getGlAccountCode())) {
                codes.putIfAbsent(account.getRowIndex(), account.getGlAccountCode());
            }
        }
        return codes;
    }

    private String termName(Supplier supplier) {
        if (supplier.getPaymentTermsRefid() == null) {
            return "";
        }
        return paymentTerms.findById(supplier.getPaymentTermsRefid())
                .map(PaymentTermsMaster::getTermsName)
                .orElse("");
    }

    private String currencyName(Supplier supplier) {
        if (supplier.getSymbolRefid() == null) {
            return "";
        }
        return symbols.findById(supplier.getSymbolRefid())
                .map(SymbolMaster::getSName)
                .orElse("");
    }

    /**
     * Header mapping pinned by legacy {@code BillMasterConvert}: the local
     * document number becomes QNE's BillCode (unlike invoices, where QNE
     * assigns the code), BillFrom concatenates name and address, and DueDate
     * and PostDate both carry the bill date — payment terms are never applied.
     */
    static QneBillRequest buildRequest(BillMaster bill, Supplier supplier,
                                       String term, String currency, List<QneBillLine> lines) {
        String billDate = QnePayloads.date(bill.getSaleDate());
        return QneBillRequest.builder()
                .billCode(bill.getCNumberDisplay())
                .billDate(billDate)
                .billFrom(QnePayloads.orEmpty(supplier.getSupplierName())
                        + "," + QnePayloads.orEmpty(supplier.getAddress1()))
                .supplier(supplier.getQneCode())
                .referenceNo(bill.getRemarks())
                .term(term)
                .currency(currency)
                .currencyRate(QnePayloads.d(bill.getCurrencyValue()))
                .description(bill.getDescription())
                .isTaxInclusive(false)
                .isRounding(false)
                .supplierInvNo(bill.getInvoiceNo())
                .dueDate(billDate)
                .postDate(billDate)
                .details(lines)
                .build();
    }
}
