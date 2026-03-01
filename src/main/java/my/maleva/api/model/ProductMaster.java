package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ProductMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "Prod_Code", length = 50, nullable = false)
    private String prodCode;

    @Column(name = "PCode_Digits")
    private Integer pcodeDigits;

    @Column(name = "PName", length = 100, nullable = false)
    private String pname;

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

    @Column(name = "MRP")
    private Double mrp;

    @Column(name = "PurchaseRate")
    private Double purchaseRate;

    @Column(name = "LandingCost")
    private Double landingCost;

    @Column(name = "SalesRate")
    private Double salesRate;

    @Column(name = "SaleRateType")
    private Boolean saleRateType;

    @Column(name = "Remarks", length = 100)
    private String remarks;

    @Column(name = "Activestatus")
    private Integer activestatus;

    @Column(name = "Sorting")
    private Integer sorting;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "IsProduct")
    private Integer isProduct;

    @OneToMany(mappedBy = "productMaster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductMasterCStock> cstocks;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (activestatus == null) {
            activestatus = 1;
        }
        if (isProduct == null) {
            isProduct = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}

