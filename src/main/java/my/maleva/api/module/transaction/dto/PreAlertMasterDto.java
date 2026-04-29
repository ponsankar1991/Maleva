package my.maleva.api.module.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreAlertMasterDto {

    private Integer id;

    // Using companyRefId to match other parts of code, though JSON property might be CompanyRefId
    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer customerMasterRefId;

    private Integer jobTypeMasterRefId;

    private Integer employeeRefId;

    private Integer cNumber;

    @Size(max = 300, message = "Port cannot exceed 300 characters")
    private String port;

    @Size(max = 300, message = "BoardingOfficerName cannot exceed 300 characters")
    private String boardingOfficerName;

    private LocalDate date;

    @Size(max = 300, message = "Vessel cannot exceed 300 characters")
    private String vessel;

    @Size(max = 50, message = "OETA cannot exceed 50 characters")
    private String oeta;

    @Size(max = 50, message = "LETA cannot exceed 50 characters")
    private String leta;

    @Size(max = 300, message = "ALLETA cannot exceed 300 characters")
    private String alleta;

    @Size(max = 300, message = "NONE cannot exceed 300 characters")
    private String none;

    @Size(max = 300, message = "ChkPort cannot exceed 300 characters")
    private String chkPort;

    @Size(max = 300, message = "ChkVessel cannot exceed 300 characters")
    private String chkVessel;

    @Size(max = 300, message = "ChkPickupDate cannot exceed 300 characters")
    private String chkPickupDate;

    @Size(max = 300, message = "CNumberDisplay cannot exceed 300 characters")
    private String cNumberDisplay;

    @Size(max = 300, message = "ChkConsolidated cannot exceed 300 characters")
    private String chkConsolidated;

    @Size(max = 300, message = "ChkDeliveryDone cannot exceed 300 characters")
    private String chkDeliveryDone;

    private LocalDate fromDate;

    private LocalDate entryDate;

    private LocalDate toDate;

    private Integer active;

    // Based on the stored procedure, PreAlert is passed as a JSON string inside the object array
    // This allows nested objects to be passed
    private String preAlert;
}
