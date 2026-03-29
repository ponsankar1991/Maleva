package my.maleva.api.module.planning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningDetailsDto {

    private Integer id;

    @NotNull(message = "Planning master reference ID is required")
    private Integer planningMasterRefId;

    @NotNull(message = "Sale order master reference ID is required")
    private Integer saleOrderMasterRefId;

    private Integer truckRefId;

    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    private String remarks;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    @Size(max = 150, message = "Origin cannot exceed 150 characters")
    private String originD;

    @Size(max = 150, message = "Destination cannot exceed 150 characters")
    private String destinationD;

    private LocalDateTime pickupDateD;

    private LocalDateTime deliveryDateD;

    @NotNull(message = "Sort by is required")
    private Integer sortBy;

    @Size(max = 200, message = "Truck name cannot exceed 200 characters")
    private String truckNameD;

    @Size(max = 200, message = "Driver name cannot exceed 200 characters")
    private String driverNameD;

    @Size(max = 500, message = "Pickup time list cannot exceed 500 characters")
    private String pickupTimeList;

    @Size(max = 500, message = "Pickup quantity list cannot exceed 500 characters")
    private String pickupQuantityList;

    @Size(max = 500, message = "Delivery quantity list cannot exceed 500 characters")
    private String deliveryQuantityList;

    @Size(max = 500, message = "Delivery time list cannot exceed 500 characters")
    private String deliveryTimeList;
}

