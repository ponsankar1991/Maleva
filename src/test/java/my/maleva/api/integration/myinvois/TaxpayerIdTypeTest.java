package my.maleva.api.integration.myinvois;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/** The idType rules LHDN's taxpayer lookup is given, ported from {@code GetIdType}. */
class TaxpayerIdTypeTest {

    @ParameterizedTest(name = "{0} is a {1}")
    @CsvSource({
            // 12 digits opening 19 or 20 — a new-format company number.
            "199801012345, BRN",
            "202301099887, BRN",
            // 12 digits whose first six are a birth date.
            "880314085321, NRIC",
            "010229101234, NRIC",
            // A letter then 7-9 digits.
            "A1234567,     PASSPORT",
            "K123456789,   PASSPORT",
            // 10-12 digits that are neither of the above.
            "1234567890,   ARMY",
            "123456789012, ARMY",
    })
    void classifiesTheFourKinds(String idValue, TaxpayerIdType expected) {
        assertThat(TaxpayerIdType.of(idValue)).isEqualTo(expected);
    }

    @Test
    @DisplayName("a 19xx company number is a BRN, not an NRIC, even though it opens with a date")
    void brnWinsOverNric() {
        // 199801012345: the first six digits (199801) also parse as 1 Aug 1998
        // under yyMMdd, so the order of the two checks is what decides this. If
        // NRIC were tested first, every company registered from 2019 onward
        // would be sent to LHDN labelled as a person.
        assertThat(TaxpayerIdType.of("199801012345")).isEqualTo(TaxpayerIdType.BRN);
    }

    @Test
    @DisplayName("dashes and case are ignored on numbers, as legacy did")
    void normalisesInput() {
        assertThat(TaxpayerIdType.of("880314-08-5321")).isEqualTo(TaxpayerIdType.NRIC);
        assertThat(TaxpayerIdType.of("  a1234567  ")).isEqualTo(TaxpayerIdType.PASSPORT);
    }

    @Test
    @DisplayName("12 digits with an impossible date are not an NRIC")
    void rejectsImpossibleBirthDate() {
        // Month 99 cannot be a birth date, so this falls through to ARMY.
        assertThat(TaxpayerIdType.of("889914085321")).isEqualTo(TaxpayerIdType.ARMY);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "AB123", "12345", "not-an-id"})
    void unrecognisedIsUnknown(String idValue) {
        assertThat(TaxpayerIdType.of(idValue)).isEqualTo(TaxpayerIdType.UNKNOWN);
    }
}
