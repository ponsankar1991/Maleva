package my.maleva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RTIMasterDto
 * Data Transfer Object for RTIMaster API layer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RTIMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer agentCompanyRefId;

    private Integer agentMasterRefId;

    @NotNull(message = "Sale Date is required")
    private LocalDateTime saleDate;

    @NotBlank(message = "CNumber Display is required")
    @Size(max = 300, message = "CNumber Display cannot exceed 300 characters")
    private String CNumberDisplay;

    @NotNull(message = "CNumber is required")
    private Integer CNumber;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    @Size(max = 100, message = "ELink cannot exceed 100 characters")
    private String eLink;

    private Integer active;

    private Integer sleeping;

    @Min(value = 0, message = "Sleeping Amount must be 0 or greater")
    private Double sleepingAmount;

    @Min(value = 0, message = "Amount must be 0 or greater")
    private Double amount;

    private LocalDateTime createdDate;

    private String createdBy;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    private Integer truckRefId;

    private Integer driverRefId;

    private Integer pickup;

    private Integer pickupCount;

    @Min(value = 0, message = "Pickup Amount must be 0 or greater")
    private Double pickupAmount;

    private Integer dropCount;

    @Min(value = 0, message = "Drop Amount must be 0 or greater")
    private Double dropAmount;

    private Integer addDrop;

    private Integer exitYN;

    private Integer exitAmount;

    @Size(max = 100, message = "EXLink cannot exceed 100 characters")
    private String exLink;

    @Size(max = 300, message = "Destination cannot exceed 300 characters")
    private String destination;

    @Size(max = 2000, message = "Seal By cannot exceed 2000 characters")
    private String sealBy;

    @Size(max = 2000, message = "Break Seal By cannot exceed 2000 characters")
    private String breakSealBy;

    private Integer lastEmployeeRefId;

    private Integer emptyDeliveryYN;

    private Integer emptyDeliveryAmount;

    @Size(max = 2000, message = "Comments cannot exceed 2000 characters")
    private String comments;

    private Integer manpw;

    @Min(value = 0, message = "Manpw Amount must be 0 or greater")
    private Double manpwAmount;

    private Integer pckHandling;

    private Integer punctuality;

    private Integer documentSub;

    private List<RTIDetailsDto> rtiDetails;
}

