package my.maleva.api.module.invoice.print;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the Crystal {@code Amountinwords} formula against lines taken from
 * real Crystal output (crReport 6 and 7): the two-space gap after the
 * symbol, hyphenated compound numbers, Indian grouping, cents, upper case.
 */
class AmountInWordsTest {

    @Test
    void matchesCrystalOutputForARoundAmount() {
        // crReport (7): "RM   ONE THOUSAND SEVEN HUNDRED ONLY"
        assertThat(AmountInWords.of("RM", new BigDecimal("1700.00")))
                .isEqualTo("RM   ONE THOUSAND SEVEN HUNDRED ONLY");
    }

    @Test
    void matchesCrystalOutputWithCentsAndHyphen() {
        // crReport (6): "SGD   FIVE HUNDRED THIRTY-SIX AND SEVENTEEN CENTS ONLY"
        assertThat(AmountInWords.of("SGD", new BigDecimal("536.17")))
                .isEqualTo("SGD   FIVE HUNDRED THIRTY-SIX AND SEVENTEEN CENTS ONLY");
        assertThat(AmountInWords.of("RM", new BigDecimal("262.05")))
                .isEqualTo("RM   TWO HUNDRED SIXTY-TWO AND FIVE CENTS ONLY");
    }

    @Test
    void lakhsAndCroresLikeTheCrystalFormula() {
        assertThat(AmountInWords.of("RM", new BigDecimal("250000.00")))
                .isEqualTo("RM   TWO LAKHS FIFTY THOUSAND ONLY");
        // legacy wrote "Lakhs" for one as well; kept
        assertThat(AmountInWords.of("RM", new BigDecimal("100000.00")))
                .isEqualTo("RM   ONE LAKHS ONLY");
        assertThat(AmountInWords.of("RM", new BigDecimal("10000000.00")))
                .isEqualTo("RM   ONE CRORE ONLY");
        assertThat(AmountInWords.of("RM", new BigDecimal("12345678.90")))
                .isEqualTo("RM   ONE CRORE TWENTY-THREE LAKHS FORTY-FIVE THOUSAND SIX HUNDRED SEVENTY-EIGHT AND NINETY CENTS ONLY");
    }

    @Test
    void zeroAndNullPrintAsCrystalDid() {
        assertThat(AmountInWords.of("RM", BigDecimal.ZERO)).isEqualTo("RM   ONLY");
        assertThat(AmountInWords.of("RM", null)).isEqualTo("RM   ONLY");
    }

    @Test
    void halfSenRoundsUpBeforeSpelling() {
        assertThat(AmountInWords.of("RM", new BigDecimal("10.005")))
                .isEqualTo("RM   TEN AND ONE CENTS ONLY");
    }
}
