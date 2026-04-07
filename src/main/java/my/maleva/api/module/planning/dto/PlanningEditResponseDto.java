package my.maleva.api.module.planning.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningEditResponseDto {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("CompanyRefId")
    private Integer companyRefId;

    @JsonProperty("UserRefId")
    private Integer userRefId;

    @JsonProperty("EmployeeRefId")
    private Integer employeeRefId;

    @JsonProperty("LastEmployeeRefId")
    private Integer lastEmployeeRefId;

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

    @JsonProperty("CNumberDisplay")
    private String cNumberDisplay;

    @JsonProperty("CNumber")
    private Integer cNumber;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("Search")
    private String search;

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

    @JsonProperty("SaleDetails")
    private List<PlanningDetailsModel> saleDetails;
}
