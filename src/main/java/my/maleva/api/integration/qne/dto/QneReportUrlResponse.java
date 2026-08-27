package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Report URL response. Legacy: QNEURLModel ("file" is lowercase on the wire).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QneReportUrlResponse {
    @JsonProperty("file")
    private String file;
}
