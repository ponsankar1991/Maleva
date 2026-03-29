package my.maleva.api.module.vessalplanning.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * VesselPlanningDetailsDto - DTO for VesselPlanningDetails
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VesselPlanningDetailsDto {

    private Integer id;

    @NotNull(message = "Vessel Planning Master Reference ID is required")
    private Integer vesselPlanningMasterRefId;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    @Size(max = 300, message = "Remarks must not exceed 300 characters")
    private String remarks;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;
}

