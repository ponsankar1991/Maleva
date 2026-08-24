package my.maleva.api.module.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.productmaster.entity.ProductMaster;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "InventoryTransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "ProductRefId", nullable = false)
    private Integer productRefId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TransactionType", length = 3, nullable = false)
    private TransactionType transactionType;

    @Column(name = "Quantity", precision = 18, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(name = "BalanceAfter", precision = 18, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    /**
     * Price of one unit on this movement, null when it was never known.
     *
     * Held on the movement rather than only on the product because the price of a
     * part changes between deliveries, and a receipt has to keep the price it
     * actually arrived at even after the next one supersedes it.
     */
    @Column(name = "UnitCost", precision = 18, scale = 4)
    private BigDecimal unitCost;

    /** Quantity * UnitCost. Null whenever unitCost is. */
    @Column(name = "TotalValue", precision = 18, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "ReferenceType", length = 50)
    private String referenceType;

    @Column(name = "ReferenceId")
    private Integer referenceId;

    @Column(name = "TruckRefId")
    private Integer truckRefId;

    @Column(name = "AssetSerialNo", length = 100)
    private String assetSerialNo;

    @Column(name = "Remarks", length = 200)
    private String remarks;

    @Column(name = "Created_By", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductRefId", insertable = false, updatable = false)
    private ProductMaster productMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TruckRefId", insertable = false, updatable = false)
    private TruckMaster truck;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}
