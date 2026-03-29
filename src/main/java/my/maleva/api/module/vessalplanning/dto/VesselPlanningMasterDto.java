package my.maleva.api.module.vessalplanning.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * VesselPlanningMasterDto - DTO for VesselPlanningMaster
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VesselPlanningMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer lastEmployeeRefId;

    @NotNull(message = "Sale Date is required")
    private LocalDateTime saleDate;

    @NotNull(message = "From Date is required")
    private LocalDate fDate;

    @NotNull(message = "To Date is required")
    private LocalDate tDate;

    @NotBlank(message = "C Number Display is required")
    @Size(max = 300, message = "C Number Display must not exceed 300 characters")
    private String cNumberDisplay;

    @NotNull(message = "C Number is required")
    private Integer cNumber;

    @Size(max = 2000, message = "Remarks must not exceed 2000 characters")
    private String remarks;

    @Size(max = 2000, message = "Search must not exceed 2000 characters")
    private String search;

    @NotNull(message = "Active status is required")
    private Integer active;

    private LocalDateTime createdDate;

    private String createdBy;

    private LocalDateTime modifiedDate;

    private String modifiedBy;
}

