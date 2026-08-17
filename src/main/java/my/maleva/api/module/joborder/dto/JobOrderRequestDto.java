package my.maleva.api.module.joborder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOrderRequestDto {

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer employeeRefId;

    private Integer truckMasterRefId;

    private Integer driverMasterRefId;

    @Size(max = 150, message = "Vendor name cannot exceed 150 characters")
    private String vendorName;

    @NotNull(message = "Job Type is required")
    private Integer jobTypeRefId;

    @Size(max = 200, message = "Problem name cannot exceed 200 characters")
    private String problemName;

    @Size(max = 200, message = "Product use cannot exceed 200 characters")
    private String productUse;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // Status is optional on creation (defaults to 1 = Open), but required for update
    private Integer statusRefId;

    private Integer priorityRefId;

    private BigDecimal odometerReading;

    private BigDecimal estimatedCost;

    private BigDecimal actualCost;

    private LocalDateTime jobDate;

    private LocalDateTime expectedCompletionDate;

    private LocalDateTime completedDate;

    private Boolean isActive;

    private Integer requestedBy;

    private java.util.List<JobOrderDetailRequestDto> details;
}
