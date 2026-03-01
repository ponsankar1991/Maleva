package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PreAlert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CustomerMasterRefId", nullable = false)
    private Integer customerMasterRefId;

    @Column(name = "EmployeeMasterRefId", nullable = false)
    private Integer employeeMasterRefId;

    @Column(name = "JobTypeMasterRefId")
    private Integer jobTypeMasterRefId;

    @Column(name = "JobStatusMasterRefId")
    private Integer jobStatusMasterRefId;

    @Column(name = "ShipName", length = 300)
    private String shipName;

    @Column(name = "Vessel", length = 300)
    private String vessel;

    @Column(name = "Commodity", length = 300)
    private String commodity;

    @Column(name = "ETA", length = 300)
    private String eta;

    @Column(name = "ETB", length = 300)
    private String etb;

    @Column(name = "ETD", length = 300)
    private String etd;

    @Column(name = "JobNo", length = 100)
    private String jobNo;

    @Column(name = "Port", length = 300)
    private String port;

    @Column(name = "Weight", length = 100)
    private String weight;

    @Column(name = "Package", length = 300)
    private String packageInfo;

    @Column(name = "AWBNo", length = 100)
    private String awbNo;

    @Column(name = "AgentName", length = 300)
    private String agentName;

    @Column(name = "AgentPhone", length = 100)
    private String agentPhone;

    @Column(name = "Remarks", length = 300)
    private String remarks;

    @Column(name = "SCN", length = 300)
    private String scn;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "PreAlertMasterRefId")
    private Integer preAlertMasterRefId;

    @Column(name = "BoardingOfficerRefId")
    private Integer boardingOfficerRefId;

    @Column(name = "BoardingOfficerName", length = 300)
    private String boardingOfficerName;

    @Column(name = "SaleOrderMasterRefId")
    private Integer saleOrderMasterRefId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PreAlertMasterRefId", insertable = false, updatable = false)
    private PreAlertMaster preAlertMaster;

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

