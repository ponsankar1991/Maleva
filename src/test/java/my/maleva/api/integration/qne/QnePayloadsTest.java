package my.maleva.api.integration.qne;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy wire-formatting rules: Json.NET's date shape, the blind
 * 100-char address slicing, and the HtmlEncode applied to stock codes.
 */
class QnePayloadsTest {

    @Test
    void datesSerializeLikeJsonNet() {
        assertThat(QnePayloads.date(LocalDateTime.of(2026, 8, 26, 0, 0)))
                .isEqualTo("2026-08-26T00:00:00");
        assertThat(QnePayloads.date(LocalDateTime.of(2026, 8, 26, 13, 5, 9)))
                .isEqualTo("2026-08-26T13:05:09");
        assertThat(QnePayloads.date(LocalDate.of(2026, 8, 26)))
                .isEqualTo("2026-08-26T00:00:00");
        assertThat(QnePayloads.date((LocalDateTime) null)).isNull();
    }

    @Test
    void shortAddressStaysInAddress1() {
        assertThat(QnePayloads.addressChunks("12 Jalan Satu"))
                .containsExactly("12 Jalan Satu", null, null, null);
        assertThat(QnePayloads.addressChunks(null))
                .containsExactly(null, null, null, null);
        assertThat(QnePayloads.addressChunks("x".repeat(100)))
                .containsExactly("x".repeat(100), null, null, null);
    }

    @Test
    void longAddressSlicesBlindIntoHundredCharChunks() {
        String address = "a".repeat(100) + "b".repeat(100) + "c".repeat(50);
        assertThat(QnePayloads.addressChunks(address))
                .containsExactly("a".repeat(100), "b".repeat(100), "c".repeat(50), null);
    }

    @Test
    void fifthChunkAndBeyondIsDiscarded() {
        String address = "x".repeat(450);
        String[] parts = QnePayloads.addressChunks(address);
        assertThat(parts).hasSize(4);
        assertThat(String.join("", parts)).hasSize(400);
    }

    @Test
    void htmlEncodeMatchesLegacyHtmlEncode() {
        assertThat(QnePayloads.htmlEncode("OIL & GAS <20\">")).isEqualTo("OIL &amp; GAS &lt;20&quot;&gt;");
        assertThat(QnePayloads.htmlEncode(null)).isNull();
    }

    @Test
    void chunksSplitsWithoutLosingElements() {
        assertThat(QnePayloads.chunks(List.of("a", "b", "c", "d", "e"), 2))
                .containsExactly(List.of("a", "b"), List.of("c", "d"), List.of("e"));
    }
}
