package my.maleva.api.module.leave.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "LeaveRequestMaster")
public class LeaveRequestMaster {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "CompanyRefId")
    private Integer companyRefId;
    
    @Column(name = "ApplicantType")
    private Integer applicantType;
    
    @Column(name = "ApplicantRefId")
    private Integer applicantRefId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApplicantRefId", insertable = false, updatable = false)
    private my.maleva.api.module.fleet.entity.DriverMaster driverMaster;
    
    @Column(name = "LeaveTypeRefId")
    private Integer leaveTypeRefId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LeaveTypeRefId", insertable = false, updatable = false)
    private LeaveTypeMaster leaveType;
    
    @Column(name = "FromDate")
    private LocalDateTime fromDate;

    @Column(name = "ToDate")
    private LocalDateTime toDate;

    @Column(name = "TotalDays")
    private Integer totalDays;
    
    @Column(name = "Reason", length = 500)
    private String reason;
    
    @Column(name = "StatusRefId")
    private Integer statusRefId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StatusRefId", insertable = false, updatable = false)
    private LeaveStatusMaster leaveStatus;
    
    @Column(name = "CreatedBy")
    private Integer createdBy;
    
    @Column(name = "ReviewedBy")
    private Integer reviewedBy;
    
    @Column(name = "ReviewRemark", length = 500)
    private String reviewRemark;
    
    @Column(name = "Active")
    private Integer active;
    
    @Column(name = "Created_Date")
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50)
    private String modifiedBy;

    @org.hibernate.annotations.Formula("(CASE WHEN ApplicantType = 1 THEN (SELECT e.EmployeeName FROM EmployeeMaster e WHERE e.Id = ApplicantRefId) " +
                                       "WHEN ApplicantType = 2 THEN (SELECT d.DriverName FROM DriverMaster d WHERE d.Id = ApplicantRefId) " +
                                       "ELSE 'Unknown' END)")
    private String applicantName;

    @org.hibernate.annotations.Formula("(CASE WHEN ApplicantType = 1 THEN (SELECT e.EmployeeName FROM EmployeeMaster e WHERE e.Id = ReviewedBy) " +
                                       "WHEN ApplicantType = 2 THEN (SELECT d.DriverName FROM DriverMaster d WHERE d.Id = ReviewedBy) " +
                                       "ELSE 'Unknown' END)")
    private String reviewedByName;
}
