package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

/**
 * QNE bill (supplier invoice) request master.
 * Legacy: BillsQneMasterInsertModel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneBillRequest {

    private String billCode;
    private String billDate;
    private String billFrom;
    private String supplier;
    private String referenceNo;
    private String term;
    private String dueDate;
    private String purchaser;
    private String project;
    private String currency;
    private double currencyRate;
    private String description;
    private String description2;
    private String notes;
    private String postDate;
    private String costCentre;
    @JsonProperty("IsTaxInclusive")
    private boolean isTaxInclusive;
    private String supplierInvNo;
    private String taxDate;
    private String roundingAdjustmentAccount;
    @JsonProperty("IsRounding")
    private boolean isRounding;
    private List<QneBillLine> details;
}
