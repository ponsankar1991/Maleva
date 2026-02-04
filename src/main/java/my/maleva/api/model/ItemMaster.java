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
 * JPA entity for ItemMaster table
 */
@Entity
@Table(name = "ItemMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "Prod_Code", length = 50, nullable = false)
    private String prodCode;

    @Column(name = "PCode_Digits", nullable = false)
    private Integer pcodeDigits;

    @Column(name = "PName", length = 100, nullable = false)
    private String pName;

    @Column(name = "PrintName", length = 100)
    private String printName;

    @Column(name = "SecondPCode", length = 100)
    private String secondPCode;

    @Column(name = "HSNCode", length = 100)
    private String hsnCode;

    @Column(name = "Tax_Code", nullable = false)
    private Integer taxCode;

    @Column(name = "UOM_Code", nullable = false)
    private Integer uomCode;

    @Column(name = "MRP", nullable = false)
    private Float mrp;

    @Column(name = "PurchaseRate", nullable = false)
    private Float purchaseRate;

    @Column(name = "LandingCost", nullable = false)
    private Float landingCost;

    @Column(name = "SalesRate", nullable = false)
    private Float salesRate;

    @Column(name = "SaleRateType", nullable = false)
    private Boolean saleRateType;

    @Column(name = "Remarks", length = 100)
    private String remarks;

    @Column(name = "Activestatus", nullable = false)
    private Integer activestatus;

    @Column(name = "Sorting", nullable = false)
    private Integer sorting;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "QNECode", length = 50)
    private String qneCode;

    @Column(name = "QNEId", length = 50)
    private String qneId;

    @Column(name = "EInvoice", length = 250)
    private String eInvoice;

    @Column(name = "SaleClassification")
    private Integer saleClassification;

    @Column(name = "SelfBilledClassification")
    private Integer selfBilledClassification;
}
