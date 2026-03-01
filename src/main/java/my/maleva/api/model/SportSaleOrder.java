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
 * SportSaleOrder Entity
 * JPA entity for SportSaleOrder table
 * Represents sport-related sales orders with comprehensive order management
 */
@Entity
@Table(name = "SportSaleOrder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportSaleOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CustomerRefId")
    private Integer customerRefId;

    @Column(name = "JobMasterRefId", nullable = false)
    private Integer jobMasterRefId;

    @Column(name = "EmployeeRefId")
    private Integer employeeRefId;

    @Column(name = "AWBNo", length = 100)
    private String awbNo;

    @Column(name = "BLCopy", length = 100)
    private String blCopy;

    @Column(name = "Quantity", length = 100)
    private String quantity;

    @Column(name = "TotalWeight", length = 100)
    private String totalWeight;

    @Column(name = "JStatus")
    private Integer jStatus;

    @Column(name = "DODescription", length = 500)
    private String doDescription;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "SaleOrderMasterRefId")
    private Integer saleOrderMasterRefId;

    @Column(name = "VehicleName", length = 100)
    private String vehicleName;

    @Column(name = "Active")
    private Integer active;

    @Column(name = "Port", length = 500)
    private String port;

    @Column(name = "DocumentPath", length = 300)
    private String documentPath;
}

