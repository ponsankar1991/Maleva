package my.maleva.api.integration.myinvois.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Body of {@code POST /api/v1.0/documentsubmissions}.
 *
 * <p>Each document travels as a Base64 string plus the SHA-256 of the same
 * bytes; LHDN recomputes the hash on what it decodes and rejects a mismatch,
 * which is why {@code MyInvoisDocumentCodec} derives both from one byte array.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSubmissionRequest {

    @JsonProperty("documents")
    private List<Document> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {

        /** Always {@code JSON} here; LHDN also accepts {@code XML}. */
        @JsonProperty("format")
        private String format;

        /** Lowercase hex SHA-256 of the UTF-8 document bytes. */
        @JsonProperty("documentHash")
        private String documentHash;

        /** The invoice number as the taxpayer knows it (SaleMaster.CNumberDisplay). */
        @JsonProperty("codeNumber")
        private String codeNumber;

        /** Base64 of the UTF-8 document bytes. */
        @JsonProperty("document")
        private String document;
    }
}
