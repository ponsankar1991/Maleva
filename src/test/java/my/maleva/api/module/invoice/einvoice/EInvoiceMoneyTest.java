package my.maleva.api.module.invoice.einvoice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The float32 columns behind the invoice cannot hold sen exactly. These tests
 * pin that every value the government sees is the value the clerk typed.
 */
class EInvoiceMoneyTest {

    @Test
    void float32NoiseRoundsBackToWhatWasTyped() {
        // 1234.56f widened to double is 1234.56005859375 — exactly what legacy sent.
        double stored = (double) 1234.56f;
        assertThat(EInvoiceMoney.of(stored)).isEqualByComparingTo("1234.56");

        assertThat(EInvoiceMoney.of((double) 63.60f)).isEqualByComparingTo("63.60");
        assertThat(EInvoiceMoney.of((double) 0.29f)).isEqualByComparingTo("0.29");
        assertThat(EInvoiceMoney.of((double) 19999.99f)).isEqualByComparingTo("19999.99");
    }

    @Test
    void alwaysTwoDecimals() {
        assertThat(EInvoiceMoney.of(1100d).toPlainString()).isEqualTo("1100.00");
        assertThat(EInvoiceMoney.of(0d).toPlainString()).isEqualTo("0.00");
        assertThat(EInvoiceMoney.zero().toPlainString()).isEqualTo("0.00");
    }

    @Test
    void nullStaysNullSoTheValidatorCanReportIt() {
        assertThat(EInvoiceMoney.of(null)).isNull();
        assertThat(EInvoiceMoney.quantity(null)).isNull();
    }

    @Test
    void quantitiesKeepTheirPrecisionButNeverFewerThanTwoDecimals() {
        // not money: 0.125 must not become 0.13
        assertThat(EInvoiceMoney.quantity(0.125).toPlainString()).isEqualTo("0.125");
        assertThat(EInvoiceMoney.quantity(2d).toPlainString()).isEqualTo("2.00");
        assertThat(EInvoiceMoney.quantity((double) 6.0f).toPlainString()).isEqualTo("6.00");
        assertThat(EInvoiceMoney.quantity((double) 1.5f).toPlainString()).isEqualTo("1.50");
    }

    @Test
    void lineTaxAndInclusiveAmountAreComputedOnTheUnroundedProductLikeTheScreen() {
        // 3 × 33.33 = 99.99; 6% = 5.9994 → 6.00 (rounded once, at the end); inclusive 105.9894 → 105.99
        BigDecimal tax = EInvoiceMoney.lineTax(new BigDecimal("3"), new BigDecimal("33.33"), new BigDecimal("6"));
        assertThat(tax).isEqualByComparingTo("6.00");
        assertThat(EInvoiceMoney.lineInclusive(new BigDecimal("3"), new BigDecimal("33.33"), new BigDecimal("6")))
                .isEqualByComparingTo("105.99");
    }

    @Test
    void agreesWithinOneSen() {
        // A stored, unrounded float32 tax and its recomputed value can round
        // to different sen; one sen of slack is the floor, not half a sen.
        assertThat(EInvoiceMoney.agrees(new BigDecimal("10.00"), new BigDecimal("10.005"))).isTrue();
        assertThat(EInvoiceMoney.agrees(new BigDecimal("10.00"), new BigDecimal("10.01"))).isTrue();
        assertThat(EInvoiceMoney.agrees(new BigDecimal("10.00"), new BigDecimal("10.02"))).isFalse();
        assertThat(EInvoiceMoney.agrees(new BigDecimal("10.00"), new BigDecimal("9.98"))).isFalse();
    }

    @Test
    void sumToleranceGrowsWithTheNumberOfIndependentlyRoundedFigures() {
        assertThat(EInvoiceMoney.sumTolerance(1)).isEqualByComparingTo("0.01");
        assertThat(EInvoiceMoney.sumTolerance(5)).isEqualByComparingTo("0.05");
        assertThat(EInvoiceMoney.sumTolerance(0)).isEqualByComparingTo("0.01");
    }

    @Test
    void flagsAmountsTheFloat32ColumnCannotHoldToTheSen() {
        assertThat(EInvoiceMoney.exceedsFloat32SenPrecision(new BigDecimal("131071.99"))).isFalse();
        assertThat(EInvoiceMoney.exceedsFloat32SenPrecision(new BigDecimal("131072.00"))).isTrue();
        assertThat(EInvoiceMoney.exceedsFloat32SenPrecision(new BigDecimal("202768.41"))).isTrue();
        assertThat(EInvoiceMoney.exceedsFloat32SenPrecision(null)).isFalse();
    }

    @Test
    void halfUpNotBankers() {
        assertThat(EInvoiceMoney.round(new BigDecimal("2.345"))).isEqualByComparingTo("2.35");
        assertThat(EInvoiceMoney.round(new BigDecimal("2.355"))).isEqualByComparingTo("2.36");
    }
}
