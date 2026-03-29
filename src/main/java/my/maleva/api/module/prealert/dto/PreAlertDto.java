package my.maleva.api.module.prealert.dto;

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

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotNull(message = "Customer Master Reference ID is required")
    private Integer customerMasterRefId;

    @NotNull(message = "Employee Master Reference ID is required")
    private Integer employeeMasterRefId;

    private Integer jobTypeMasterRefId;

    private Integer jobStatusMasterRefId;

    @Size(max = 300, message = "Ship Name cannot exceed 300 characters")
    private String shipName;

    @Size(max = 300, message = "Vessel cannot exceed 300 characters")
    private String vessel;

    @Size(max = 300, message = "Commodity cannot exceed 300 characters")
    private String commodity;

    @Size(max = 300, message = "ETA cannot exceed 300 characters")
    private String eta;

    @Size(max = 300, message = "ETB cannot exceed 300 characters")
    private String etb;

    @Size(max = 300, message = "ETD cannot exceed 300 characters")
    private String etd;

    @Size(max = 100, message = "JobNo cannot exceed 100 characters")
    private String jobNo;

    @Size(max = 300, message = "Port cannot exceed 300 characters")
    private String port;

    @Size(max = 100, message = "Weight cannot exceed 100 characters")
    private String weight;

    @Size(max = 300, message = "Package cannot exceed 300 characters")
    private String packageInfo;

    @Size(max = 100, message = "AWB No cannot exceed 100 characters")
    private String awbNo;

    @Size(max = 300, message = "Agent Name cannot exceed 300 characters")
    private String agentName;

    @Size(max = 100, message = "Agent Phone cannot exceed 100 characters")
    private String agentPhone;

    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    private String remarks;

    @Size(max = 300, message = "SCN cannot exceed 300 characters")
    private String scn;

    private Integer active;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private Integer preAlertMasterRefId;

    private Integer boardingOfficerRefId;

    @Size(max = 300, message = "Boarding Officer Name cannot exceed 300 characters")
    private String boardingOfficerName;

    private Integer saleOrderMasterRefId;
}

