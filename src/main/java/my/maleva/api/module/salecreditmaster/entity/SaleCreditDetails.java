package my.maleva.api.module.salecreditmaster.entity;

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
 * SaleCreditDetails Entity
 * JPA entity for SaleCreditDetails table
 */
@Entity
@Table(name = "SaleCreditDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleCreditDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SaleCreditMasterRefId", nullable = false)
    private Integer saleCreditMasterRefId;

    @Column(name = "ItemMasterRefId", nullable = false)
    private Integer itemMasterRefId;

    @Column(name = "MRP", nullable = false)
    private Double mrp;

    @Column(name = "PurchaseRate", nullable = false)
    private Double purchaseRate;

    @Column(name = "ItemQty", nullable = false)
    private Double itemQty;

    @Column(name = "DiscPer", nullable = false)
    private Double discPer;

    @Column(name = "DiscAmount", nullable = false)
    private Double discAmount;

    @Column(name = "LandingCost", nullable = false)
    private Double landingCost;

    @Column(name = "TaxPercent", nullable = false)
    private Double taxPercent;

    @Column(name = "TaxAmount", nullable = false)
    private Double taxAmount;

    @Column(name = "SalesRate", nullable = false)
    private Double salesRate;

    @Column(name = "NetSalesRate", nullable = false)
    private Double netSalesRate;

    @Column(name = "Amount", nullable = false)
    private Double amount;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "CurrencyValue")
    private Double currencyValue;

    @Column(name = "ActualAmount")
    private Double actualAmount;
}

