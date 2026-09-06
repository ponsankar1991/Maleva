package my.maleva.api.integration.myinvois;

import my.maleva.api.integration.myinvois.dto.DocumentSubmissionRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LHDN recomputes the hash over the bytes it decodes from the Base64. These
 * tests pin that the two are derived from the same bytes and that the JSON is
 * in the shape LHDN accepted from the legacy system.
 */
class MyInvoisDocumentCodecTest {

    private final MyInvoisDocumentCodec codec = new MyInvoisDocumentCodec();

    @Test
    void hashAndBase64ComeFromTheSameBytes() throws Exception {
        Map<String, Object> document = Map.of("_D", "urn:x", "Amount", new BigDecimal("12.30"));

        MyInvoisDocumentCodec.EncodedDocument encoded = codec.encode(document, "INV1");

        byte[] decoded = Base64.getDecoder().decode(encoded.base64());
        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo(encoded.json());

        byte[] expectedHash = MessageDigest.getInstance("SHA-256").digest(decoded);
        assertThat(encoded.documentHash()).isEqualTo(HexFormat.of().formatHex(expectedHash));
        assertThat(encoded.documentHash()).hasSize(64).isLowerCase();
    }

    @Test
    void jsonIsCompactWithPlainDecimalsAndNoNulls() {
        Map<String, Object> document = new java.util.LinkedHashMap<>();
        document.put("big", new BigDecimal("1234567890.10"));
        document.put("tiny", new BigDecimal("0.05"));
        document.put("absent", null);

        String json = codec.toJson(document);

        assertThat(json).isEqualTo("{\"big\":1234567890.10,\"tiny\":0.05}");
    }

    @Test
    void submissionRequestWrapsTheDocumentAsLhdnExpects() {
        MyInvoisDocumentCodec.EncodedDocument encoded = codec.encode(Map.of("k", "v"), "INV000000042");

        DocumentSubmissionRequest request = encoded.toSubmissionRequest();

        assertThat(request.getDocuments()).hasSize(1);
        DocumentSubmissionRequest.Document doc = request.getDocuments().get(0);
        assertThat(doc.getFormat()).isEqualTo("JSON");
        assertThat(doc.getCodeNumber()).isEqualTo("INV000000042");
        assertThat(doc.getDocumentHash()).isEqualTo(encoded.documentHash());
        assertThat(doc.getDocument()).isEqualTo(encoded.base64());
    }
}
