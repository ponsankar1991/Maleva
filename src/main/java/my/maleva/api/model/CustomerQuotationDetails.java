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
 * JPA entity for CustomerQuotationDetails table
 */
@Entity
@Table(name = "CustomerQuotationDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerQuotationDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CustomerQuotationRefId", nullable = false)
    private Integer customerQuotationRefId;

    @Column(name = "ItemMasterRefId")
    private Integer itemMasterRefId;

    @Column(name = "MRP")
    private Integer mrp;

    @Column(name = "PurchaseRate")
    private Integer purchaseRate;

    @Column(name = "ItemQty")
    private Integer itemQty;

    @Column(name = "DiscPer", nullable = false)
    private Float discPer;

    @Column(name = "DiscAmount", nullable = false)
    private Float discAmount;

    @Column(name = "LandingCost", nullable = false)
    private Float landingCost;

    @Column(name = "TaxPercent", nullable = false)
    private Float taxPercent;

    @Column(name = "TaxAmount", nullable = false)
    private Float taxAmount;

    @Column(name = "SalesRate", nullable = false)
    private Float salesRate;

    @Column(name = "NetSalesRate", nullable = false)
    private Float netSalesRate;

    @Column(name = "Amount", nullable = false)
    private Float amount;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "CurrencyValue")
    private Float currencyValue;

    @Column(name = "ActualAmount")
    private Float actualAmount;

    @Column(name = "SDRemarks", length = 300)
    private String sdRemarks;

    @Column(name = "TaxRefId")
    private Integer taxRefId;

    @Column(name = "ProductName", length = 300)
    private String productName;

    @Column(name = "RowType", length = 300)
    private String rowType;

    @Column(name = "GroupKey", length = 300)
    private String groupKey;

    @Column(name = "OrderNo")
    private Integer orderNo;
}
