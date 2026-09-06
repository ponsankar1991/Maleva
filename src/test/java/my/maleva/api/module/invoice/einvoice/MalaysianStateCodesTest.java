package my.maleva.api.module.invoice.einvoice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MalaysianStateCodesTest {

    @Test
    void canonicalNamesMapToLhdnCodes() {
        assertThat(MalaysianStateCodes.subentityCode("MYS", "Selangor")).contains("10");
        assertThat(MalaysianStateCodes.subentityCode("MYS", "JOHOR")).contains("01");
        assertThat(MalaysianStateCodes.subentityCode("MYS", "Kuala Lumpur")).contains("14");
    }

    @Test
    void realCustomerSpellingsAreRecognised() {
        // Each of these exists in the customer master and failed under the legacy map.
        assertThat(MalaysianStateCodes.subentityCode("MYS", "PULAU PINANG")).contains("07");
        assertThat(MalaysianStateCodes.subentityCode("MYS", "WILAYAH PERSEKUTUAN KUALA LUMPUR")).contains("14");
        assertThat(MalaysianStateCodes.subentityCode("MYS", "W.P. Kuala Lumpur")).contains("14");
        assertThat(MalaysianStateCodes.subentityCode("MYS", "  selangor  ")).contains("10");
        assertThat(MalaysianStateCodes.subentityCode("MYS", "Negeri   Sembilan")).contains("05");
    }

    @Test
    void unknownMalaysianStateIsRefusedNotGuessed() {
        assertThat(MalaysianStateCodes.subentityCode("MYS", "Atlantis")).isEmpty();
        assertThat(MalaysianStateCodes.subentityCode("MYS", "")).isEmpty();
        assertThat(MalaysianStateCodes.subentityCode("MYS", null)).isEmpty();
    }

    @Test
    void foreignAddressesPassTheStateThroughAsTyped() {
        assertThat(MalaysianStateCodes.subentityCode("SGP", "NOT APPLICABLE")).contains("NOT APPLICABLE");
        assertThat(MalaysianStateCodes.subentityCode("CHN", " PUDONG ")).contains("PUDONG");
        assertThat(MalaysianStateCodes.subentityCode("SGP", "")).isEmpty();
    }

    @Test
    void malaysiaIsRecognisedByCodeOrName() {
        assertThat(MalaysianStateCodes.isMalaysia("MYS")).isTrue();
        assertThat(MalaysianStateCodes.isMalaysia("malaysia")).isTrue();
        assertThat(MalaysianStateCodes.isMalaysia("SGP")).isFalse();
        assertThat(MalaysianStateCodes.isMalaysia(null)).isFalse();
    }
}
