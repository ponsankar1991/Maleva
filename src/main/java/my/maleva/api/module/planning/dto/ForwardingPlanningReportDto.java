package my.maleva.api.module.planning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForwardingPlanningReportDto {
    private String lorryNo;
    private String driverName;
    private String driverNumber;
    private String agentName;
    private String contact;
    private String fromLocation;
    private String eta; // Formatted time, e.g. "0800HRS" or raw LocalDateTime string
    private String jobType;
    private String port;
    private String remarks;
    private String fullRoute;
    private Integer marqisStatus;
}
