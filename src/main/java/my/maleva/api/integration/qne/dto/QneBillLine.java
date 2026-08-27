package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * QNE bill (supplier invoice) request detail line.
 * Legacy: BillsQneDetailInsertModel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneBillLine {

    private String account;
    private String description;
    private String referenceNo;
    private double amount;
    private String taxCode;
    @JsonProperty("IsTaxInclusive")
    private boolean isTaxInclusive;
    private String project;
    private String costCentre;
}
