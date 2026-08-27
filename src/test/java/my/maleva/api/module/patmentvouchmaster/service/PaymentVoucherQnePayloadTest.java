package my.maleva.api.module.patmentvouchmaster.service;

import my.maleva.api.integration.qne.dto.QnePaymentVoucherLine;
import my.maleva.api.integration.qne.dto.QnePaymentVoucherRequest;
import my.maleva.api.module.patmentvouchmaster.entity.PaymentVoucherMaster;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy PaymentVoucherConvert mapping — currency hardcoded 'RM'
 * (the legacy SQL literal) alongside the voucher's real currency rate.
 */
class PaymentVoucherQnePayloadTest {

    @Test
    void mapsLegacyPaymentVoucherConvertFields() {
        PaymentVoucherMaster voucher = new PaymentVoucherMaster();
        voucher.setPaymentVoucherDate(LocalDateTime.of(2026, 8, 26, 0, 0));
        voucher.setPayTo("TNB BERHAD");
        voucher.setRefNo("PV-55");
        voucher.setDescription("Utilities");
        voucher.setCurrencyValue(1.0f);
        voucher.setBankCharges(0.0f);

        QnePaymentVoucherRequest request = PaymentVoucherQneService.buildRequest(
                voucher, "310-0001",
                List.of(QnePaymentVoucherLine.builder().account("902-0000").description("Electricity").amount(410.0).build()));

        assertThat(request.getPaymentDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getPayByAccount()).isEqualTo("310-0001");
        assertThat(request.getPayTo()).isEqualTo("TNB BERHAD");
        assertThat(request.getReferenceNo()).isEqualTo("PV-55");
        assertThat(request.getCurrency()).isEqualTo("RM");
        assertThat(request.getCurrencyRate()).isEqualTo(1.0);
        assertThat(request.getDescription()).isEqualTo("Utilities");
        assertThat(request.getTaxDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getChequeDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getBouncedChequeDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getChequePreparedDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.isBouncedCheque()).isFalse();
        assertThat(request.isCancelled()).isFalse();
        assertThat(request.isPostDatedCheque()).isFalse();
        assertThat(request.isTaxInclusive()).isFalse();
        assertThat(request.isRounding()).isFalse();
        assertThat(request.getDetails()).hasSize(1);
        assertThat(request.getDetails().get(0).getAccount()).isEqualTo("902-0000");
    }
}
