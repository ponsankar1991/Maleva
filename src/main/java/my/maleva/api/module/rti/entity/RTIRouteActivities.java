package my.maleva.api.module.rti.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "RTIRouteActivities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RTIRouteActivities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "RTIMasterRefId", nullable = false)
    private Integer rtiMasterRefId;

    @Column(name = "SequenceNo", nullable = false)
    private Short sequenceNo;

    @Column(name = "LocationName", nullable = false, length = 255)
    private String locationName;

    @Column(name = "ActivityType", nullable = false, length = 50)
    private String activityType;

    @Column(name = "EmployeeRefId")
    private Integer employeeRefId;

    @Column(name = "AgentMobileNo", length = 50)
    private String agentMobileNo;

    @Column(name = "Status", nullable = false)
    private Byte status;

    @Column(name = "PlannedDateTime")
    private LocalDateTime plannedDateTime;

    @Column(name = "ETA")
    private LocalDateTime eta;

    @Column(name = "Remarks", length = 1000)
    private String remarks;

    @Column(name = "Active", nullable = false)
    private Boolean active;

    @Column(name = "FullRoute")
    private String fullRoute;

    @Column(name = "DriverNumber")
    private String driverNumber;

    @Column(name = "MarqisStatus")
    private Integer marqisStatus;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", nullable = false, length = 50)
    private String modifiedBy;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (status == null) status = 0;
        if (active == null) active = true;
        if (createdBy == null || createdBy.isBlank()) createdBy = "SYSTEM";
        if (modifiedBy == null || modifiedBy.isBlank()) modifiedBy = createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
        if (modifiedBy == null || modifiedBy.isBlank()) modifiedBy = "SYSTEM";
    }
}
