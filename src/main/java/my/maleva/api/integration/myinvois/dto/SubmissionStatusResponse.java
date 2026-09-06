package my.maleva.api.integration.myinvois.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Body of {@code GET /documentsubmissions/{submissionUid}}.
 *
 * <p>Timestamps are kept as the ISO strings LHDN sends and parsed by the
 * caller. Legacy declared them as non-nullable {@code DateTime}, so a summary
 * for a document still being validated — where {@code dateTimeValidated} is
 * null — threw during parsing, and that throw happened before the accepted
 * UUID had been saved. Strings cannot throw.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionStatusResponse {

    @JsonProperty("submissionUid")
    private String submissionUid;

    @JsonProperty("documentCount")
    private Integer documentCount;

    @JsonProperty("dateTimeReceived")
    private String dateTimeReceived;

    /** {@code in progress}, {@code valid}, {@code partially valid}, {@code invalid}. */
    @JsonProperty("overallStatus")
    private String overallStatus;

    @JsonProperty("documentSummary")
    private List<DocumentSummary> documentSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocumentSummary {

        @JsonProperty("uuid")
        private String uuid;

        @JsonProperty("submissionUid")
        private String submissionUid;

        /** Present once validated; forms the public share link with the uuid. */
        @JsonProperty("longId")
        private String longId;

        @JsonProperty("internalId")
        private String internalId;

        @JsonProperty("typeName")
        private String typeName;

        @JsonProperty("typeVersionName")
        private String typeVersionName;

        @JsonProperty("issuerTin")
        private String issuerTin;

        @JsonProperty("issuerName")
        private String issuerName;

        @JsonProperty("receiverId")
        private String receiverId;

        @JsonProperty("receiverName")
        private String receiverName;

        @JsonProperty("dateTimeIssued")
        private String dateTimeIssued;

        @JsonProperty("dateTimeReceived")
        private String dateTimeReceived;

        /** Null while LHDN is still validating. */
        @JsonProperty("dateTimeValidated")
        private String dateTimeValidated;

        @JsonProperty("totalPayableAmount")
        private BigDecimal totalPayableAmount;

        @JsonProperty("totalExcludingTax")
        private BigDecimal totalExcludingTax;

        @JsonProperty("totalDiscount")
        private BigDecimal totalDiscount;

        @JsonProperty("totalNetAmount")
        private BigDecimal totalNetAmount;

        /** {@code Submitted}, {@code Valid}, {@code Invalid}, {@code Cancelled}. */
        @JsonProperty("status")
        private String status;

        @JsonProperty("cancelDateTime")
        private String cancelDateTime;

        @JsonProperty("rejectRequestDateTime")
        private String rejectRequestDateTime;

        @JsonProperty("documentStatusReason")
        private String documentStatusReason;

        @JsonProperty("createdByUserId")
        private String createdByUserId;
    }
}
