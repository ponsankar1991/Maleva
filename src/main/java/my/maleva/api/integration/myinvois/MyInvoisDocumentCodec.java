package my.maleva.api.integration.myinvois;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import my.maleva.api.integration.myinvois.dto.DocumentSubmissionRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

/**
 * Turns a UBL document object into what LHDN wants on the wire: compact JSON,
 * its SHA-256, and its Base64 — all three from ONE byte array.
 *
 * <p>LHDN decodes the Base64 and recomputes the hash; if the two disagree the
 * submission is rejected. Deriving both from a single {@code byte[]} makes
 * disagreement impossible. The mapper here is private to this class, not the
 * application's shared one, because the shared mapper's settings (date
 * formats, empty-string coercions) must never leak into a document whose
 * bytes are hashed.
 *
 * <p>Serialisation rules, matching what LHDN has accepted from the legacy
 * system: no whitespace; null properties omitted (that is how an optional
 * element is "not sent"); {@link java.math.BigDecimal} written as a plain
 * number, never in exponent form.
 */
@Component
public class MyInvoisDocumentCodec {

    public static final String FORMAT_JSON = "JSON";

    private final ObjectMapper documentMapper;

    public MyInvoisDocumentCodec() {
        this.documentMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .disable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /** Serialise, hash and encode one document. */
    public EncodedDocument encode(Object document, String codeNumber) {
        String json;
        try {
            json = documentMapper.writeValueAsString(document);
        } catch (Exception ex) {
            throw new IllegalStateException("UBL document could not be serialised", ex);
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return new EncodedDocument(
                codeNumber,
                json,
                HexFormat.of().formatHex(sha256(bytes)),
                Base64.getEncoder().encodeToString(bytes));
    }

    /** The JSON as it will be hashed — for tests and for logging on rejection. */
    public String toJson(Object document) {
        try {
            return documentMapper.writeValueAsString(document);
        } catch (Exception ex) {
            throw new IllegalStateException("UBL document could not be serialised", ex);
        }
    }

    private static byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available in this JVM", ex);
        }
    }

    /**
     * One document ready for submission.
     *
     * @param codeNumber   the invoice number LHDN indexes the document by
     * @param json         the exact JSON that was hashed and encoded
     * @param documentHash lowercase hex SHA-256 of {@code json}'s UTF-8 bytes
     * @param base64       Base64 of the same bytes
     */
    public record EncodedDocument(String codeNumber, String json, String documentHash, String base64) {

        public DocumentSubmissionRequest toSubmissionRequest() {
            return DocumentSubmissionRequest.builder()
                    .documents(List.of(DocumentSubmissionRequest.Document.builder()
                            .format(FORMAT_JSON)
                            .documentHash(documentHash)
                            .codeNumber(codeNumber)
                            .document(base64)
                            .build()))
                    .build();
        }
    }
}
