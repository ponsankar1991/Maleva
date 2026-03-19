package my.maleva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleOrderDetails Entity
 * JPA entity for SaleOrderDetails table
 * Represents line items for sale orders
 * 
 * Performance Optimizations:
 * - Index on SaleOrderMasterRefId for JOIN with SaleOrderMaster
 * - Index on ItemMasterRefId for JOIN with ItemMaster
 * - Composite index for filtering by order ID and sorting
 */
@Entity
@Table(name = "SaleOrderDetails", indexes = {
    // Foreign key join for SaleOrderMaster lookup
    @Index(name = "idx_sale_order_master_ref", columnList = "SaleOrderMasterRefId", unique = false),
    
    // Foreign key join for ItemMaster lookup
    @Index(name = "idx_item_master_ref", columnList = "ItemMasterRefId", unique = false),
    
    // Composite index for filtering details by order ID
    @Index(name = "idx_sale_order_item", columnList = "SaleOrderMasterRefId,ItemMasterRefId", unique = false)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SaleOrderMasterRefId", nullable = false)
    private Integer saleOrderMasterRefId;

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

    @Column(name = "SDRemarks", length = 300)
    private String sdRemarks;

    @Column(name = "TaxRefId")
    private Integer taxRefId;
}

