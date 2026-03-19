package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PlanningMasterViewModel - Response DTO for planning master data
 * Maps to the .NET PLANINGMasterViewModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningMasterViewModel {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("SDId")
    private Integer sdId;

    @JsonProperty("PLANINGNo")
    private Integer planningNo;

    @JsonProperty("PLANINGNoDisplay")
    private String planningNoDisplay;

    @JsonProperty("FDate")
    private String fDate;

    @JsonProperty("TDate")
    private String tDate;

    @JsonProperty("SFDate")
    private String sFDate;

    @JsonProperty("STDate")
    private String sTDate;

    @JsonProperty("SaleDate")
    private String saleDate;

    @JsonProperty("SSaleDate")
    private String sSaleDate;

    @JsonProperty("PLANINGDate")
    private String planningDate;

    @JsonProperty("CNumberDisplay")
    private String cNumberDisplay;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("Active")
    private Integer active;

    @JsonProperty("Created_Date")
    private String createdDate;

    @JsonProperty("Created_By")
    private String createdBy;

    @JsonProperty("Modified_Date")
    private String modifiedDate;

    @JsonProperty("Modified_By")
    private String modifiedBy;
}
