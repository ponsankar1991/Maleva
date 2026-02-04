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
 * JPA entity for BillsOrderDetails table
 */
@Entity
@Table(name = "BillsOrderDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "BillsOrderMasterRefId", nullable = false)
    private Integer billsOrderMasterRefId;

    @Column(name = "AccountMasterRefId", nullable = false)
    private Integer accountMasterRefId;

    @Column(name = "MRP", nullable = false)
    private Float mrp;

    @Column(name = "PurchaseRate", nullable = false)
    private Float purchaseRate;

    @Column(name = "ItemQty", nullable = false)
    private Float itemQty;

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

    @Column(name = "RemarksD", length = 300)
    private String remarksD;

    @Column(name = "CurrencyValue", nullable = false)
    private Float currencyValue;

    @Column(name = "ActualAmount", nullable = false)
    private Float actualAmount;

    @Column(name = "ProductRefId")
    private Integer productRefId;

    @Column(name = "QuoteValue")
    private Float quoteValue;

    @Column(name = "SerialNo", length = 150)
    private String serialNo;
}
