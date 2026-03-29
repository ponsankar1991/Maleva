package my.maleva.api.module.planning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningMasterDto {

    private Integer id;

    @NotNull(message = "Company reference ID is required")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer lastEmployeeRefId;

    @NotNull(message = "Sale date is required")
    private LocalDateTime saleDate;

    @NotNull(message = "From date is required")
    private LocalDateTime fDate;

    @NotNull(message = "To date is required")
    private LocalDateTime tDate;

    @NotBlank(message = "CNumber display is required")
    @Size(max = 300, message = "CNumber display cannot exceed 300 characters")
    private String cNumberDisplay;

    @NotNull(message = "CNumber is required")
    private Integer cNumber;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    @Size(max = 2000, message = "Search field cannot exceed 2000 characters")
    private String search;

    @NotNull(message = "Active status is required")
    private Integer active;

    private LocalDateTime createdDate;

    @Size(max = 50, message = "Created by cannot exceed 50 characters")
    private String createdBy;

    private LocalDateTime modifiedDate;

    @Size(max = 50, message = "Modified by cannot exceed 50 characters")
    private String modifiedBy;

    // Child details
    private List<PlanningDetailsDto> planningDetails;
}

