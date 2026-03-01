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
 * StockIn Entity
 * JPA entity for StockIn table
 * Represents stock inbound records with comprehensive warehouse management
 */
@Entity
@Table(name = "StockIn")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockIn {

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

    @Column(name = "SaleOrderMasterRefId")
    private Integer saleOrderMasterRefId;

    @Column(name = "StockDate", nullable = false)
    private LocalDateTime stockDate;

    @Column(name = "CNumberDisplay", nullable = false, length = 300)
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "NumberOfPackages", nullable = false)
    private Integer numberOfPackages;

    @Column(name = "PortMasterRefId", nullable = false)
    private Integer portMasterRefId;

    @Column(name = "Barcode", length = 200)
    private String barcode;

    @Column(name = "BarcodeLabelDisplay", length = 200)
    private String barcodeLabelDisplay;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", nullable = false, length = 50)
    private String modifiedBy;

    @Column(name = "Status", nullable = false)
    private Integer status;

    @Column(name = "Warehousedate")
    private LocalDateTime warehouseDate;
}

