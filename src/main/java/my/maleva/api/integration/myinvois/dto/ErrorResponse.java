package my.maleva.api.integration.myinvois.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LHDN's error envelope, {@code {"error": {code, message, target, propertyPath, details[]}}}.
 * The same shape appears at the top level of a 4xx body and inside
 * {@code rejectedDocuments[].error}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {

    @JsonProperty("error")
    private ErrorDetail error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDetail {

        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;

        @JsonProperty("target")
        private String target;

        @JsonProperty("propertyPath")
        private String propertyPath;

        /** Nested detail rows; LHDN nests them one level for structural errors. */
        @JsonProperty("details")
        private List<ErrorDetail> details;
    }
}
