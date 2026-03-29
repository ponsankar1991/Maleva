package my.maleva.api.module.rti.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RTIMaster Entity
 * Represents a Road Transport Infrastructure Master record
 * Implements the SP_RTIMaster stored procedure logic
 */
@Entity
@Table(name = "RTIMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RTIMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "UserRefId")
    private Integer userRefId;

    @Column(name = "EmployeeRefId")
    private Integer employeeRefId;

    @Column(name = "AgentCompanyRefId")
    private Integer agentCompanyRefId;

    @Column(name = "AgentMasterRefId")
    private Integer agentMasterRefId;

    @Column(name = "SaleDate", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String CNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer CNumber;

    @Column(name = "Remarks", length = 2000)
    private String remarks;

    @Column(name = "ELink", length = 100)
    private String eLink;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Sleeping", nullable = false)
    private Integer sleeping;

    @Column(name = "SleepingAmount")
    private Double sleepingAmount;

    @Column(name = "Amount")
    private Double amount;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "TruckRefid")
    private Integer truckRefId;

    @Column(name = "DriverRefid")
    private Integer driverRefId;

    @Column(name = "Pickup", nullable = false)
    private Integer pickup;

    @Column(name = "PickupCount", nullable = false)
    private Integer pickupCount;

    @Column(name = "PickupAmount")
    private Double pickupAmount;

    @Column(name = "DropCount", nullable = false)
    private Integer dropCount;

    @Column(name = "DropAmount")
    private Double dropAmount;

    @Column(name = "AddDrop", nullable = false)
    private Integer addDrop;

    @Column(name = "ExiTYN", nullable = false)
    private Integer exitYN;

    @Column(name = "ExitAmount")
    private Integer exitAmount;

    @Column(name = "EXLink", length = 100)
    private String exLink;

    @Column(name = "Destination", length = 300)
    private String destination;

    @Column(name = "SealBy", length = 2000)
    private String sealBy;

    @Column(name = "BreakSealBy", length = 2000)
    private String breakSealBy;

    @Column(name = "LastEmployeeRefId")
    private Integer lastEmployeeRefId;

    @Column(name = "EmptyDeliveryYN")
    private Integer emptyDeliveryYN;

    @Column(name = "EmptyDeliveryAmount")
    private Integer emptyDeliveryAmount;

    @Column(name = "Comments", length = 2000)
    private String comments;

    @Column(name = "Manpw")
    private Integer manpw;

    @Column(name = "ManpwAmount")
    private Double manpwAmount;

    @Column(name = "PckHandling")
    private Integer pckHandling;

    @Column(name = "Punctuality")
    private Integer punctuality;

    @Column(name = "DocumentSub")
    private Integer documentSub;

    @OneToMany(mappedBy = "rtiMaster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RTIDetails> rtiDetails;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (active == null) {
            active = 0;
        }
        if (sleeping == null) {
            sleeping = 0;
        }
        if (pickup == null) {
            pickup = 0;
        }
        if (pickupCount == null) {
            pickupCount = 0;
        }
        if (dropCount == null) {
            dropCount = 0;
        }
        if (addDrop == null) {
            addDrop = 0;
        }
        if (exitYN == null) {
            exitYN = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}

