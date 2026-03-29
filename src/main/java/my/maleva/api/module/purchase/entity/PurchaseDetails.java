package my.maleva.api.module.purchase.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PurchaseDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "PurchaseMasterRefId", nullable = false)
    private Integer purchaseMasterRefId;

    @Column(name = "ProductMasterRefId", nullable = false)
    private Integer productMasterRefId;

    @Column(name = "MRP")
    private Double mrp;

    @Column(name = "PurchaseRate")
    private Double purchaseRate;

    @Column(name = "ItemQty")
    private Double itemQty;

    @Column(name = "DiscPer")
    private Double discPer;

    @Column(name = "DiscAmount")
    private Double discAmount;

    @Column(name = "LandingCost")
    private Double landingCost;

    @Column(name = "TaxPercent")
    private Double taxPercent;

    @Column(name = "TaxAmount")
    private Double taxAmount;

    @Column(name = "SalesRate")
    private Double salesRate;

    @Column(name = "NetSalesRate")
    private Double netSalesRate;

    @Column(name = "Amount")
    private Double amount;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "RemarksD", length = 300)
    private String remarksD;

    @Column(name = "CurrencyValue")
    private Double currencyValue;

    @Column(name = "ActualAmount")
    private Double actualAmount;

    @Column(name = "ProductName", length = 300)
    private String productName;

    @Column(name = "ProductCode", length = 300)
    private String productCode;
}

