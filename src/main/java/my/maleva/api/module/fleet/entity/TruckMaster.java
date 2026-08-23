package my.maleva.api.module.fleet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TruckMaster Entity
 * JPA entity for TruckMaster table
 * Represents truck information with maintenance tracking
 */
@Entity
@Table(name = "TruckMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "TruckName", nullable = false, length = 200)
    private String truckName;

    @Column(name = "TruckNumber", nullable = false, length = 100)
    private String truckNumber;

    @Column(name = "CNumberDisplay", nullable = false, length = 300)
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "TruckType", nullable = false, length = 100)
    private String truckType;

    @Column(name = "Latitude", length = 50)
    private String latitude;

    @Column(name = "longitude", length = 50)
    private String longitude;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", nullable = false, length = 50)
    private String modifiedBy;

    @Column(name = "RotexMyExp")
    private LocalDate rotexMyExp;

    @Column(name = "RotexSGExp")
    private LocalDate rotexSGExp;

    @Column(name = "PuspacomExp")
    private LocalDate puspacomExp;

    @Column(name = "InsuratnceExp")
    private LocalDate insuranceExp;

    @Column(name = "BonamExp")
    private LocalDate bonamExp;

    @Column(name = "ApadExp")
    private LocalDate apadExp;

    @Column(name = "RotexMyExp1")
    private LocalDate rotexMyExp1;

    @Column(name = "RotexSGExp1")
    private LocalDate rotexSGExp1;

    @Column(name = "PuspacomExp1")
    private LocalDate puspacomExp1;

    @Column(name = "TruckNumber1", length = 100)
    private String truckNumber1;

    @Column(name = "ServiceExp")
    private LocalDate serviceExp;

    @Column(name = "AlignmentExp")
    private LocalDate alignmentExp;

    @Column(name = "GreeceExp")
    private LocalDate greeceExp;

    @Column(name = "AccountRefid", nullable = false)
    private Integer accountRefid;

    @Column(name = "GearOilExp")
    private LocalDate gearOilExp;

    @Column(name = "PTPStickerExp")
    private LocalDate ptpStickerExp;

    @Column(name = "AlignmentLast")
    private LocalDate alignmentLast;

    @Column(name = "GreeceLast")
    private LocalDate greeceLast;

    @Column(name = "GearOilLast")
    private LocalDate gearOilLast;

    @Column(name = "ServiceLast")
    private LocalDate serviceLast;

    @Column(name = "SIDExp", length = 500)
    private String sidExp;

    @Column(name = "VehicleType", length = 300)
    private String vehicleType;

    /**
     * Whether this truck belongs to Maleva itself, as opposed to a
     * subcontractor's vehicle kept in the same TruckMaster table.
     * 1 = Maleva-owned, 0 = not. On the live data 69 of 71 active trucks are 1.
     */
    @Column(name = "MalevaTruck")
    private Integer malevaTruck;
}

