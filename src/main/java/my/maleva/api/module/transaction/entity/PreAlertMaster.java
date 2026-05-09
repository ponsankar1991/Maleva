package my.maleva.api.module.transaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PreAlertMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreAlertMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CustomerMasterRefId")
    private Integer customerMasterRefId;

    @Column(name = "JobTypeMasterRefId")
    private Integer jobTypeMasterRefId;

    @Column(name = "FromDate")
    private LocalDate fromDate;

    @Column(name = "ToDate")
    private LocalDate toDate;

    @Column(name = "Port", length = 300)
    private String port;

    @Column(name = "Vessel", length = 300)
    private String vessel;

    @Column(name = "OETA", length = 300)
    private String oeta;

    @Column(name = "LETA", length = 300)
    private String leta;

    @Column(name = "ALLETA", length = 300)
    private String alleta;

    @Column(name = "NONE", length = 300)
    private String none;

    @Column(name = "ChkPort", length = 300)
    private String chkPort;

    @Column(name = "ChkVessel", length = 300)
    private String chkVessel;

    @Column(name = "ChkPickupDate", length = 300)
    private String chkPickupDate;

    @Column(name = "ChkConsolidated", length = 300)
    private String chkConsolidated;

    @Column(name = "ChkDeliveryDone", length = 300)
    private String chkDeliveryDone;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "CNumber")
    private Integer CNumber;

    @Column(name = "CNumberDisplay", length = 300)
    private String CNumberDisplay;

    @Column(name = "EntryDate")
    private LocalDate entryDate;

    @Column(name = "SaleOrderMasterRefId")
    private Integer saleOrderMasterRefId;

    @OneToMany(mappedBy = "preAlertMaster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PreAlert> preAlerts;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (active == null) {
            active = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
