package my.maleva.api.module.prealert.dto;

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

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer customerMasterRefId;

    private Integer jobTypeMasterRefId;

    private LocalDate fromDate;

    private LocalDate toDate;

    @Size(max = 300, message = "Port cannot exceed 300 characters")
    private String port;

    @Size(max = 300, message = "Vessel cannot exceed 300 characters")
    private String vessel;

    @Size(max = 300, message = "OETA cannot exceed 300 characters")
    private String oeta;

    @Size(max = 300, message = "LETA cannot exceed 300 characters")
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

    @Size(max = 300, message = "ChkConsolidated cannot exceed 300 characters")
    private String chkConsolidated;

    @Size(max = 300, message = "ChkDeliveryDone cannot exceed 300 characters")
    private String chkDeliveryDone;

    private Integer active;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private Integer CNumber;

    @Size(max = 300, message = "CNumberDisplay cannot exceed 300 characters")
    private String CNumberDisplay;

    private LocalDate entryDate;

    private Integer saleOrderMasterRefId;
}


