package my.maleva.api.module.payment.service;

import my.maleva.api.integration.qne.dto.QnePayBillRequest;
import my.maleva.api.module.payment.entity.Payment;
import my.maleva.api.module.supplier.entity.Supplier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy PaymentConvert mapping — master only, every cheque date
 * carrying the payment date, and the currency name deliberately absent
 * (legacy selected it but never mapped it).
 */
class PaymentQnePayloadTest {

    @Test
    void mapsLegacyPaymentConvertFields() {
        Payment payment = new Payment();
        payment.setPaymentDate(LocalDateTime.of(2026, 8, 26, 0, 0));
        payment.setRefNumber("PB-77");
        payment.setCurrencyValue(1.0f);
        payment.setBankCharges(3.0f);
        payment.setAmount(new BigDecimal("999.99"));

        Supplier supplier = new Supplier();
        supplier.setSupplierName("PORT SERVICES SDN BHD");
        supplier.setQneCode("S-0003");

        QnePayBillRequest request =
                PaymentQneService.buildRequest(payment, supplier, "310-0001");

        assertThat(request.getPaymentDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getPayByAccount()).isEqualTo("310-0001");
        assertThat(request.getSupplier()).isEqualTo("S-0003");
        assertThat(request.getPayTo()).isEqualTo("PORT SERVICES SDN BHD");
        assertThat(request.getReferenceNo()).isEqualTo("PB-77");
        assertThat(request.getCurrencyRate()).isEqualTo(1.0);
        assertThat(request.getBankChargesAmount()).isEqualTo(3.0);
        assertThat(request.getTaxDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getChequeDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getBouncedChequeDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getChequePreparedDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getTotalAmount()).isEqualTo(999.99);
        assertThat(request.isBouncedCheque()).isFalse();
        assertThat(request.isCancelled()).isFalse();
        assertThat(request.isPostDatedCheque()).isFalse();
        assertThat(request.isTaxInclusive()).isFalse();
        assertThat(request.isRounding()).isFalse();
    }
}
