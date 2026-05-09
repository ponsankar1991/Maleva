package my.maleva.api.module.transaction.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreAlertDto {

    /**
     * PreAlert Detail ID
     * - For INSERT: Set to 0 or null (new detail row)
     * - For UPDATE: Set to existing PreAlert Id > 0
     */
    @JsonProperty("Id")
    @JsonAlias({"id"})
    private Integer id;

    @JsonProperty("CompanyRefId")
    @JsonAlias({"companyRefId"})
    private Integer companyRefId;

    @JsonProperty("CustomerMasterRefId")
    @JsonAlias({"customerMasterRefId"})
    private Integer customerMasterRefId;

    @JsonProperty("EmployeeMasterRefId")
    @JsonAlias({"employeeMasterRefId"})
    private Integer employeeMasterRefId;

    @JsonProperty("JobTypeMasterRefId")
    @JsonAlias({"jobTypeMasterRefId"})
    private Integer jobTypeMasterRefId;

    @JsonProperty("JobStatusMasterRefId")
    @JsonAlias({"jobStatusMasterRefId"})
    private Integer jobStatusMasterRefId;

    @Size(max = 300, message = "Ship Name cannot exceed 300 characters")
    @JsonProperty("ShipName")
    @JsonAlias({"shipName"})
    private String shipName;

    @Size(max = 300, message = "Vessel cannot exceed 300 characters")
    @JsonProperty("Vessel")
    @JsonAlias({"vessel"})
    private String vessel;

    @Size(max = 300, message = "Commodity cannot exceed 300 characters")
    @JsonProperty("Commodity")
    @JsonAlias({"commodity"})
    private String commodity;

    @Size(max = 300, message = "ETA cannot exceed 300 characters")
    @JsonProperty("ETA")
    @JsonAlias({"eta"})
    private String eta;

    @Size(max = 300, message = "ETB cannot exceed 300 characters")
    @JsonProperty("ETB")
    @JsonAlias({"etb"})
    private String etb;

    @Size(max = 300, message = "ETD cannot exceed 300 characters")
    @JsonProperty("ETD")
    @JsonAlias({"etd"})
    private String etd;

    @Size(max = 100, message = "JobNo cannot exceed 100 characters")
    @JsonProperty("JobNo")
    @JsonAlias({"jobNo"})
    private String jobNo;

    @Size(max = 300, message = "Port cannot exceed 300 characters")
    @JsonProperty("Port")
    @JsonAlias({"port"})
    private String port;

    @Size(max = 100, message = "Weight cannot exceed 100 characters")
    @JsonProperty("Weight")
    @JsonAlias({"weight"})
    private String weight;

    @Size(max = 300, message = "Package cannot exceed 300 characters")
    @JsonProperty("Package")
    @JsonAlias({"packageInfo"})
    private String packageInfo;

    @Size(max = 100, message = "AWB No cannot exceed 100 characters")
    @JsonProperty("AWBNo")
    @JsonAlias({"awbNo"})
    private String awbNo;

    @Size(max = 300, message = "Agent Name cannot exceed 300 characters")
    @JsonProperty("AgentName")
    @JsonAlias({"agentName"})
    private String agentName;

    @Size(max = 100, message = "Agent Phone cannot exceed 100 characters")
    @JsonProperty("AgentPhone")
    @JsonAlias({"agentPhone"})
    private String agentPhone;

    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    @JsonProperty("Remarks")
    @JsonAlias({"remarks"})
    private String remarks;

    @Size(max = 300, message = "SCN cannot exceed 300 characters")
    @JsonProperty("SCN")
    @JsonAlias({"scn"})
    private String scn;

    @JsonProperty("Active")
    @JsonAlias({"active"})
    private Integer active;

    @JsonProperty("CreatedDate")
    @JsonAlias({"createdDate"})
    private LocalDateTime createdDate;

    @JsonProperty("ModifiedDate")
    @JsonAlias({"modifiedDate"})
    private LocalDateTime modifiedDate;

    @JsonProperty("PreAlertMasterRefId")
    @JsonAlias({"preAlertMasterRefId"})
    private Integer preAlertMasterRefId;

    @JsonProperty("BoardingOfficerRefId")
    @JsonAlias({"boardingOfficerRefId"})
    private Integer boardingOfficerRefId;

    @Size(max = 300, message = "Boarding Officer Name cannot exceed 300 characters")
    @JsonProperty("BoardingOfficerName")
    @JsonAlias({"boardingOfficerName"})
    private String boardingOfficerName;

    @JsonProperty("SaleOrderMasterRefId")
    @JsonAlias({"saleOrderMasterRefId"})
    private Integer saleOrderMasterRefId;
}
