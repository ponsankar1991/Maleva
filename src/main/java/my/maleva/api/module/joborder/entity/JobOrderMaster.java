package my.maleva.api.module.joborder.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "JobOrderMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobOrderMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "CNumberDisplay", nullable = false, length = 50)
    private String cNumberDisplay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeRefId")
    private EmployeeMaster employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TruckMasterRefId")
    private TruckMaster truck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DriverMasterRefId")
    private DriverMaster driver;

    @Column(name = "VendorName", length = 150)
    private String vendorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JobTypeRefId", nullable = false)
    private JobOrderTypeMaster jobType;

    @Column(name = "ProblemName", length = 200)
    private String problemName;

    @Column(name = "ProductUse", length = 200)
    private String productUse;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StatusRefId", nullable = false)
    private JobOrderStatusMaster status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PriorityRefId")
    private JobOrderPriorityMaster priority;

    @Column(name = "OdometerReading", precision = 10, scale = 2)
    private BigDecimal odometerReading;

    @Column(name = "EstimatedCost", precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "ActualCost", precision = 12, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "JobDate", nullable = false)
    private LocalDateTime jobDate = LocalDateTime.now();

    @Column(name = "ExpectedCompletionDate")
    private LocalDateTime expectedCompletionDate;

    @Column(name = "CompletedDate")
    private LocalDateTime completedDate;

    @Column(name = "CreatedBy")
    private Integer createdBy;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "ModifiedBy")
    private Integer modifiedBy;

    @Column(name = "ModifiedDate")
    private LocalDateTime modifiedDate;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "RequestedBy")
    private Integer requestedBy;

    @OneToMany(mappedBy = "jobOrderMaster", fetch = FetchType.LAZY)
    private java.util.List<JobOrderDetail> details;
}
