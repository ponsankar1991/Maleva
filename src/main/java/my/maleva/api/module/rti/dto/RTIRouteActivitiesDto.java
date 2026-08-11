package my.maleva.api.module.rti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RTIRouteActivitiesDto {
    private Integer id;
    private Integer companyRefId;
    private Integer rtiMasterRefId;
    private Short sequenceNo;
    private String locationName;
    private String activityType;
    private Integer employeeRefId;
    private String agentMobileNo;
    private Byte status;
    private LocalDateTime plannedDateTime;
    private LocalDateTime eta;
    private String remarks;
    private Integer rtiId;
    private Integer cNumber;
    private String rtinumber;
    private String fullRoute;
    private String driverNumber;
    private Integer marqisStatus;
    private Boolean active;
    private LocalDateTime createdDate;
}
