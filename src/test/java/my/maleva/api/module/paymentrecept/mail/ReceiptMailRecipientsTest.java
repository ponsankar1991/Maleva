package my.maleva.api.module.paymentrecept.mail;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptMailRecipientsTest {

    @Test
    void splitsOnCommaAndSemicolonKeepingOrderAndDroppingDuplicates() {
        List<String> out = ReceiptMailRecipients.split(List.of(
                " a@x.com, B@y.com ;", "", "b@Y.com", "c@z.com"));
        assertThat(out).containsExactly("a@x.com", "B@y.com", "c@z.com");
    }

    @Test
    void nullAndBlankGiveAnEmptyList() {
        assertThat(ReceiptMailRecipients.split((String) null)).isEmpty();
        assertThat(ReceiptMailRecipients.split("  ;, ")).isEmpty();
        assertThat(ReceiptMailRecipients.split((List<String>) null)).isEmpty();
    }

    @Test
    void reportsAddressesThatAreNotShapedLikeOne() {
        assertThat(ReceiptMailRecipients.isValid("accounts@maleva.com.my")).isTrue();
        assertThat(ReceiptMailRecipients.isValid("no-at-sign")).isFalse();
        assertThat(ReceiptMailRecipients.isValid("a@b")).isFalse();
        assertThat(ReceiptMailRecipients.invalid(List.of("ok@x.com", "bad", "x@y.z")))
                .containsExactly("bad", "x@y.z");
    }
}
