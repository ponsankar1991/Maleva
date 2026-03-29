package my.maleva.api.module.fleet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SpeedReportDto - DTO for SpeedReport
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeedReportDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotNull(message = "Truck Reference ID is required")
    private Integer truckRefId;

    @Size(max = 200, message = "Vehicle must not exceed 200 characters")
    private String vehicle;

    private LocalDateTime time;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 100, message = "Count must not exceed 100 characters")
    private String count;

    @Size(max = 100, message = "Filled must not exceed 100 characters")
    private String filled;

    @Size(max = 200, message = "Driver must not exceed 200 characters")
    private String driver;

    private LocalDateTime createdDate;
}

