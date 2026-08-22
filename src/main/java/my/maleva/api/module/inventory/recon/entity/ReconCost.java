package my.maleva.api.module.inventory.recon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.maleva.api.module.productmaster.entity.ProductMaster;
import my.maleva.api.module.supplier.entity.Supplier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One line of repair spend on a recon job.
 *
 * A repair is rarely a single figure: an in-house job is labour plus whatever
 * parts came out of the store, an outside job is the vendor invoice plus the
 * transport to get the unit there and back. Holding them as lines is what lets
 * the total on the job be justified rather than just asserted.
 */
@Entity
@Table(name = "InventoryReconCost")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "ReconRefId", nullable = false)
    private Integer reconRefId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CostType", length = 20, nullable = false)
    private ReconCostType costType;

    @Column(name = "Description", length = 200)
    private String description;

    /**
     * Set when this line is a part taken from Maleva's own store. The service
     * issues that quantity out of stock when the line is added, so a part used
     * on a recon does not stay on the shelf in the balance.
     */
    @Column(name = "ProductRefId")
    private Integer productRefId;

    @Column(name = "Quantity", precision = 18, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(name = "Rate", precision = 18, scale = 2, nullable = false)
    private BigDecimal rate;

    /** Always quantity * rate - computed by the service, not supplied. */
    @Column(name = "Amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "SupplierRefId")
    private Integer supplierRefId;

    @Column(name = "DocNo", length = 50)
    private String docNo;

    @Column(name = "DocDate")
    private LocalDateTime docDate;

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
    @JoinColumn(name = "SupplierRefId", insertable = false, updatable = false)
    private Supplier supplier;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        if (quantity == null) {
            quantity = BigDecimal.ONE;
        }
        if (rate == null) {
            rate = BigDecimal.ZERO;
        }
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
    }
}
