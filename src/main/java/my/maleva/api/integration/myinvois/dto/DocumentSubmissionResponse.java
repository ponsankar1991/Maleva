package my.maleva.api.integration.myinvois.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Body of a 202 from {@code POST /documentsubmissions}.
 *
 * <p>LHDN spells the field {@code submissionUid}. The legacy DTO declared
 * {@code submissionUID} and only worked because Json.NET binds names
 * case-insensitively; Jackson does not, so the exact name matters here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentSubmissionResponse {

    @JsonProperty("submissionUid")
    private String submissionUid;

    @JsonProperty("acceptedDocuments")
    private List<AcceptedDocument> acceptedDocuments;

    @JsonProperty("rejectedDocuments")
    private List<RejectedDocument> rejectedDocuments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AcceptedDocument {

        /** LHDN's document UUID — becomes SaleMaster.EInvoiceUid. */
        @JsonProperty("uuid")
        private String uuid;

        @JsonProperty("invoiceCodeNumber")
        private String invoiceCodeNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RejectedDocument {

        @JsonProperty("invoiceCodeNumber")
        private String invoiceCodeNumber;

        @JsonProperty("error")
        private ErrorResponse.ErrorDetail error;
    }
}
