package my.maleva.api.module.rti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for driver RTI report
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRtiReportRequest {
    private Integer comid;
    private Integer driverId;
    private Integer truckId;
    private LocalDate fromDate;
    private LocalDate toDate;
}

