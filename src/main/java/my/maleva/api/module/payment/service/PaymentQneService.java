package my.maleva.api.module.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QnePayBillRequest;
import my.maleva.api.integration.qne.dto.QnePaymentResponse;
import my.maleva.api.module.master.repository.BankMasterRepository;
import my.maleva.api.module.payment.entity.Payment;
import my.maleva.api.module.payment.repository.PaymentRepository;
import my.maleva.api.module.supplier.entity.Supplier;
import my.maleva.api.module.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * QNE push for supplier payments — the Java port of the QNE side of legacy
 * {@code PaymentServices.PaymentConvert}, posting to PayBills. Deliberately
 * master-only: the legacy detail loop was commented out, and legacy selected
 * a currency it never mapped, so only the currency <em>rate</em> travels —
 * both behaviours are contract with the data already in QNE.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentQneService {

    private final QneGateway gateway;
    private final PaymentRepository payments;
    private final SupplierRepository suppliers;
    private final BankMasterRepository banks;

    public QnePushResult push(Integer paymentId, Integer companyId) {
        Payment payment = payments.findById(paymentId).orElse(null);
        if (payment == null) {
            return QnePushResult.localError(404, "Payment not found: " + paymentId);
        }
        if (!Objects.equals(payment.getCompanyRefId(), companyId)) {
            return QnePushResult.localError(403, "Payment does not belong to company " + companyId);
        }
        if (!QnePayloads.isBlank(payment.getQneCode())) {
            return QnePushResult.alreadyPushed(payment.getQneId(), payment.getQneCode(),
                    "Payment already exists in QNE as " + payment.getQneCode());
        }
        Supplier supplier = suppliers.findById(payment.getSupplierRefId()).orElse(null);
        if (supplier == null) {
            return QnePushResult.localError(409, "Supplier not found for payment " + paymentId);
        }
        if (QnePayloads.isBlank(supplier.getQneCode())) {
            return QnePushResult.localError(409,
                    "Supplier '" + supplier.getSupplierName() + "' is not in QNE yet — push the supplier first");
        }
        String payByAccount = payment.getBankRefId() == null ? "" :
                banks.findById(payment.getBankRefId())
                        .map(bank -> QnePayloads.orEmpty(bank.getQneCode()))
                        .orElse("");
        if (QnePayloads.isBlank(payByAccount)) {
            return QnePushResult.localError(409,
                    "The payment's bank account has no QNE code mapped on BankMaster — seed it first");
        }

        QneCall<QnePaymentResponse> call = gateway.createPayBill(
                buildRequest(payment, supplier, payByAccount));
        if (!call.success()) {
            return QnePushResult.rejected(call.message());
        }

        payments.claimQneIdentity(payment.getId(), call.data().getId(), call.data().getPaymentCode());
        return QnePushResult.ok(call.data().getId(), call.data().getPaymentCode(), null,
                "Payment pushed to QNE as " + call.data().getPaymentCode());
    }

    /**
     * Mapping pinned by legacy {@code PaymentConvert}: PayTo is the supplier
     * name, every cheque-related date carries the payment date, and all the
     * boolean flags are false.
     */
    static QnePayBillRequest buildRequest(Payment payment, Supplier supplier, String payByAccount) {
        String paymentDate = QnePayloads.date(payment.getPaymentDate());
        return QnePayBillRequest.builder()
                .paymentDate(paymentDate)
                .payByAccount(payByAccount)
                .supplier(supplier.getQneCode())
                .payTo(supplier.getSupplierName())
                .referenceNo(payment.getRefNumber())
                .currencyRate(QnePayloads.d(payment.getCurrencyValue()))
                .bankChargesAmount(QnePayloads.d(payment.getBankCharges()))
                .taxDate(paymentDate)
                .chequeDate(paymentDate)
                .bouncedChequeDate(paymentDate)
                .chequePreparedDate(paymentDate)
                .totalAmount(QnePayloads.d(payment.getAmount()))
                .isBouncedCheque(false)
                .isCancelled(false)
                .isPostDatedCheque(false)
                .isTaxInclusive(false)
                .isRounding(false)
                .build();
    }
}
