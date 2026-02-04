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
 * JPA entity for CustomerQuotation table
 */
@Entity
@Table(name = "CustomerQuotation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CustomerMasterRefId", nullable = false)
    private Integer customerMasterRefId;

    @Column(name = "JobMasterRefId", nullable = false)
    private Integer jobMasterRefId;

    @Column(name = "ItemMasterRefId", nullable = false)
    private Integer itemMasterRefId;

    @Column(name = "MRP", nullable = false)
    private Float mrp;

    @Column(name = "PurchaseRate", nullable = false)
    private Float purchaseRate;

    @Column(name = "LandingCost", nullable = false)
    private Float landingCost;

    @Column(name = "SalesRate", nullable = false)
    private Float salesRate;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "start", nullable = false)
    private Float start;

    @Column(name = "ends", nullable = false)
    private Float ends;

    @Column(name = "UOM", length = 50, nullable = false)
    private String uom;

    @Column(name = "port", length = 50, nullable = false)
    private String port;

    @Column(name = "IsTransport")
    private Integer isTransport;
}
