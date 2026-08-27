package my.maleva.api.module.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.integration.qne.QneCall;
import my.maleva.api.integration.qne.QneGateway;
import my.maleva.api.integration.qne.QnePayloads;
import my.maleva.api.integration.qne.QnePushResult;
import my.maleva.api.integration.qne.dto.QnePayBillRequest;
import my.maleva.api.integration.qne.dto.QnePaymentResponse;
import my.maleva.api.module.master.entity.BankMaster;
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
 *
 * <p>Two legacy INNER JOINs are deliberately not reproduced. It joined
 * SymbolMaster and PaymentTermsMaster and used neither in the payload, so a
 * supplier missing either one vanished from the result set and
 * {@code resultsm[0]} threw — swallowed by an inner catch that returned no
 * message, making a failed push look like nothing happening. Checked against
 * MalevanewDemo 2026-08-27: no live payment is currently dropped that way, so
 * removing them changes no outcome, only the failure mode.
 *
 * <p><b>Nothing legacy could push is refused here.</b> The local checks stop
 * only at things that would have failed there too — a missing payment, the
 * wrong company, a payment already in QNE, or a bank row that does not exist.
 * A blank QNE code on the bank or supplier is logged and sent anyway, because
 * QNE accepts it: PY000002155 was pushed on PETTY CASH S2, which has no code,
 * and came back as BPV2603/358. QNE stays the authority on what it will take,
 * and its rejection message is passed straight back to the caller.
 *
 * <p><b>Known data issue, deliberately not "fixed" here:</b> the Pay Bills
 * screen never sets {@code CurrencyValue}, so all 3,231 payments carry 0 and
 * every push sends {@code currencyRate: 0} — 2,431 of which QNE has accepted,
 * so it evidently ignores or defaults the field. Bills, by contrast, send a
 * real rate. Coalescing 0 to 1 here would be more defensible arithmetic but
 * would diverge from 2,431 records already in QNE, so it is a decision for
 * whoever owns the integration rather than a silent change.
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
            // Legacy sent whatever was there and let QNE decide, so this is a
            // warning rather than a refusal — QNE's own rejection message
            // reaches the caller if it does object.
            log.warn("Supplier '{}' has no QNE code; pushing payment {} without one",
                    supplier.getSupplierName(), payment.getCNumberDisplay());
        }
        // The bank row itself must exist — legacy read it through an INNER JOIN,
        // so a missing one emptied the result set and threw on `resultsm[0]`,
        // which its catch turned into a silent no-op.
        BankMaster bank = payment.getBankRefId() == null
                ? null : banks.findById(payment.getBankRefId()).orElse(null);
        if (bank == null) {
            return QnePushResult.localError(409,
                    "No bank account on payment " + payment.getCNumberDisplay());
        }

        // An unmapped QNE code is NOT refused. QNE demonstrably accepts a blank
        // payByAccount: PY000002155 went across on PETTY CASH S2, which has no
        // code, and came back as BPV2603/358. Blocking it here would break a
        // flow that works today — the missing mapping is worth reporting, not
        // worth stopping a payment for.
        String payByAccount = QnePayloads.orEmpty(bank.getQneCode());
        if (QnePayloads.isBlank(payByAccount)) {
            log.warn("Bank '{}' has no QNE code mapped; payment {} goes to QNE without a "
                            + "pay-by account. Map it on BankMaster so QNE posts against the "
                            + "right ledger account.",
                    bank.getName(), payment.getCNumberDisplay());
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
                // Carried straight from the payment, as legacy did. Note that
                // the Pay Bills screen never sets CurrencyValue, so in practice
                // this is 0 for every payment — see the class comment.
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
