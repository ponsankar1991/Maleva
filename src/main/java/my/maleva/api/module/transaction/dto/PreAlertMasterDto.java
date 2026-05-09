package my.maleva.api.module.transaction.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreAlertMasterDto {

    @JsonProperty("Id")
    @JsonAlias({"id"})
    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    @JsonProperty("CompanyRefId")
    @JsonAlias({"companyRefId"})
    private Integer companyRefId;

    @JsonProperty("CustomerMasterRefId")
    @JsonAlias({"customerMasterRefId"})
    private Integer customerMasterRefId;

    @JsonProperty("JobTypeMasterRefId")
    @JsonAlias({"jobTypeMasterRefId"})
    private Integer jobTypeMasterRefId;

    @JsonProperty("EmployeeRefId")
    @JsonAlias({"employeeRefId"})
    private Integer employeeRefId;

    @JsonProperty("EmployeeMasterRefId")
    @JsonAlias({"employeeMasterRefId"})
    private Integer employeeMasterRefId;

    @JsonProperty("CNumber")
    @JsonAlias({"cNumber"})
    private Integer cNumber;

    @Size(max = 300, message = "Port cannot exceed 300 characters")
    @JsonProperty("Port")
    @JsonAlias({"port"})
    private String port;

    @Size(max = 300, message = "BoardingOfficerName cannot exceed 300 characters")
    @JsonProperty("BoardingOfficerName")
    @JsonAlias({"boardingOfficerName"})
    private String boardingOfficerName;

    @JsonProperty("Date")
    @JsonAlias({"date"})
    private LocalDate date;

    @Size(max = 300, message = "Vessel cannot exceed 300 characters")
    @JsonProperty("Vessel")
    @JsonAlias({"vessel"})
    private String vessel;

    @Size(max = 50, message = "OETA cannot exceed 50 characters")
    @JsonProperty("OETA")
    @JsonAlias({"oeta"})
    private String oeta;

    @Size(max = 50, message = "LETA cannot exceed 50 characters")
    @JsonProperty("LETA")
    @JsonAlias({"leta"})
    private String leta;

    @Size(max = 300, message = "ALLETA cannot exceed 300 characters")
    @JsonProperty("ALLETA")
    @JsonAlias({"alleta"})
    private String alleta;

    @Size(max = 300, message = "NONE cannot exceed 300 characters")
    @JsonProperty("NONE")
    @JsonAlias({"none"})
    private String none;

    @Size(max = 300, message = "ChkPort cannot exceed 300 characters")
    @JsonProperty("ChkPort")
    @JsonAlias({"chkPort"})
    private String chkPort;

    @Size(max = 300, message = "ChkVessel cannot exceed 300 characters")
    @JsonProperty("ChkVessel")
    @JsonAlias({"chkVessel"})
    private String chkVessel;

    @Size(max = 300, message = "ChkPickupDate cannot exceed 300 characters")
    @JsonProperty("ChkPickupDate")
    @JsonAlias({"chkPickupDate"})
    private String chkPickupDate;

    @Size(max = 300, message = "CNumberDisplay cannot exceed 300 characters")
    @JsonProperty("CNumberDisplay")
    @JsonAlias({"cNumberDisplay"})
    private String cNumberDisplay;

    @Size(max = 300, message = "ChkConsolidated cannot exceed 300 characters")
    @JsonProperty("ChkConsolidated")
    @JsonAlias({"chkConsolidated"})
    private String chkConsolidated;

    @Size(max = 300, message = "ChkDeliveryDone cannot exceed 300 characters")
    @JsonProperty("ChkDeliveryDone")
    @JsonAlias({"chkDeliveryDone"})
    private String chkDeliveryDone;

    @JsonProperty("FromDate")
    @JsonAlias({"fromDate"})
    private LocalDate fromDate;

    @JsonProperty("EntryDate")
    @JsonAlias({"entryDate"})
    private LocalDate entryDate;

    @JsonProperty("ToDate")
    @JsonAlias({"toDate"})
    private LocalDate toDate;

    @JsonProperty("Active")
    @JsonAlias({"active"})
    private Integer active;

    @JsonProperty("CreatedDate")
    @JsonAlias({"createdDate"})
    private LocalDateTime createdDate;

    @JsonProperty("ModifiedDate")
    @JsonAlias({"modifiedDate"})
    private LocalDateTime modifiedDate;

    @JsonProperty("SaleOrderMasterRefId")
    @JsonAlias({"saleOrderMasterRefId"})
    private Integer saleOrderMasterRefId;

    @JsonProperty("PreAlert")
    @JsonAlias({"preAlert"})
    private List<PreAlertDto> preAlertRows;

    @JsonProperty("preAlert")
    @JsonAlias({"PreAlertJson"})
    private String preAlert;
}
