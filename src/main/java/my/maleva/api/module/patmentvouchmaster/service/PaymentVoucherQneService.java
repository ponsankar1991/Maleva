package my.maleva.api.module.patmentvouchmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QnePaymentResponse;
import my.maleva.api.integration.qne.dto.QnePaymentVoucherLine;
import my.maleva.api.integration.qne.dto.QnePaymentVoucherRequest;
import my.maleva.api.module.accounting.entity.GLAccounts;
import my.maleva.api.module.accounting.repository.GLAccountsRepository;
import my.maleva.api.module.master.repository.BankMasterRepository;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherDetails;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherMaster;
import my.maleva.api.module.patmentvouchmaster.repository.PaymentVoucherDetailsRepository;
import my.maleva.api.module.patmentvouchmaster.repository.PaymentVoucherMasterRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * QNE push for payment vouchers — the Java port of the QNE side of legacy
 * {@code PaymentVoucherServices.PaymentVoucherConvert}. Detail lines resolve
 * their account through {@code GLAccounts.RowIndex = AccountGroupRefId} and
 * keep the legacy detail-id order.
 *
 * <p>Currency is the literal {@code RM} — the legacy SQL hardcoded it while
 * still sending the voucher's real currency rate. Internally inconsistent for
 * foreign-currency vouchers, but it is what every voucher in QNE was posted
 * with, so changing it is a business decision, not a migration one.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVoucherQneService {

    private final QneGateway gateway;
    private final PaymentVoucherMasterRepository vouchers;
    private final PaymentVoucherDetailsRepository voucherDetails;
    private final BankMasterRepository banks;
    private final GLAccountsRepository glAccounts;

    public QnePushResult push(Integer voucherId, Integer companyId) {
        PaymentVoucherMaster voucher = vouchers.findById(voucherId).orElse(null);
        if (voucher == null) {
            return QnePushResult.localError(404, "Payment voucher not found: " + voucherId);
        }
        if (!Objects.equals(voucher.getCompanyRefId(), companyId)) {
            return QnePushResult.localError(403, "Payment voucher does not belong to company " + companyId);
        }
        if (voucher.getActive() == null || voucher.getActive() != 1) {
            return QnePushResult.localError(409, "Payment voucher " + voucherId + " is not active");
        }
        if (!QnePayloads.isBlank(voucher.getQneCode())) {
            return QnePushResult.alreadyPushed(voucher.getQneId(), voucher.getQneCode(),
                    "Payment voucher already exists in QNE as " + voucher.getQneCode());
        }
        String payByAccount = voucher.getPaymentById() == null ? "" :
                banks.findById(voucher.getPaymentById())
                        .map(bank -> QnePayloads.orEmpty(bank.getQneCode()))
                        .orElse("");
        if (QnePayloads.isBlank(payByAccount)) {
            return QnePushResult.localError(409,
                    "The voucher's bank account has no QNE code mapped on BankMaster — seed it first");
        }

        List<PaymentVoucherDetails> details =
                voucherDetails.findByPaymentVoucherMasterRefIdOrderByIdAsc(voucher.getId());
        Map<Integer, String> accounts = accountCodesByRowIndex(
                details.stream().map(PaymentVoucherDetails::getAccountGroupRefId).filter(Objects::nonNull).distinct().toList());
        List<Integer> unmapped = details.stream()
                .map(PaymentVoucherDetails::getAccountGroupRefId)
                .filter(ref -> ref == null || !accounts.containsKey(ref))
                .distinct()
                .toList();
        if (!unmapped.isEmpty()) {
            // Legacy's INNER join dropped these lines silently; refuse instead.
            return QnePushResult.localError(409,
                    "No GL account is mapped for account refs " + unmapped
                            + " — the voucher would post to QNE incomplete");
        }

        List<QnePaymentVoucherLine> lines = new ArrayList<>();
        for (PaymentVoucherDetails detail : details) {
            lines.add(QnePaymentVoucherLine.builder()
                    .account(accounts.get(detail.getAccountGroupRefId()))
                    .description(detail.getDescription())
                    .amount(QnePayloads.d(detail.getAmount()))
                    .build());
        }

        QneCall<QnePaymentResponse> call = gateway.createPaymentVoucher(
                buildRequest(voucher, payByAccount, lines));
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }

        vouchers.claimQneIdentity(voucher.getId(), call.data().getId(), call.data().getPaymentCode());
        return QnePushResult.ok(call.data().getId(), call.data().getPaymentCode(), null,
                "Payment voucher pushed to QNE as " + call.data().getPaymentCode());
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

    /** Mapping pinned by legacy {@code PaymentVoucherConvert}. */
    static QnePaymentVoucherRequest buildRequest(PaymentVoucherMaster voucher, String payByAccount,
                                                 List<QnePaymentVoucherLine> lines) {
        String paymentDate = QnePayloads.date(voucher.getPaymentVoucherDate());
        return QnePaymentVoucherRequest.builder()
                .paymentDate(paymentDate)
                .payByAccount(payByAccount)
                .payTo(voucher.getPayTo())
                .referenceNo(voucher.getRefNo())
                .currency("RM")
                .currencyRate(QnePayloads.d(voucher.getCurrencyValue()))
                .description(voucher.getDescription())
                .bankChargesAmount(QnePayloads.d(voucher.getBankCharges()))
                .taxDate(paymentDate)
                .chequeDate(paymentDate)
                .bouncedChequeDate(paymentDate)
                .chequePreparedDate(paymentDate)
                .isBouncedCheque(false)
                .isCancelled(false)
                .isPostDatedCheque(false)
                .isTaxInclusive(false)
                .isRounding(false)
                .details(lines)
                .build();
    }
}
