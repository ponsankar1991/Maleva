package my.maleva.api.model;

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

import java.time.LocalDateTime;

/**
 * JPA entity for FuelEntry table
 */
@Entity
@Table(name = "FuelEntry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelEntry {

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

    @Column(name = "LastEmployeeRefId")
    private Integer lastEmployeeRefId;

    @Column(name = "TruckRefid")
    private Integer truckRefid;

    @Column(name = "DriverRefId")
    private Integer driverRefId;

    @Column(name = "SaleDate", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "Remarks", length = 2000)
    private String remarks;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "FStatus")
    private Integer fStatus;

    @Column(name = "Aliter", nullable = false)
    private Float aliter;

    @Column(name = "AAmount", nullable = false)
    private Float aAmount;

    @Column(name = "Pliter", nullable = false)
    private Float pliter;

    @Column(name = "PRate", nullable = false)
    private Float pRate;

    @Column(name = "PAmount", nullable = false)
    private Float pAmount;

    @Column(name = "Gliter", nullable = false)
    private Float gliter;

    @Column(name = "GAmount", nullable = false)
    private Float gAmount;

    @Column(name = "DPliter", nullable = false)
    private Float dPliter;

    @Column(name = "DPAmount", nullable = false)
    private Float dpAmount;

    @Column(name = "DGliter", nullable = false)
    private Float dGliter;

    @Column(name = "DGAmount", nullable = false)
    private Float dgAmount;

    @Column(name = "FilePath", length = 3000)
    private String filePath;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;
}
