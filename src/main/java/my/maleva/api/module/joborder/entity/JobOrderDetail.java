package my.maleva.api.module.joborder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "JobOrderDetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "JobOrderMasterRefId", nullable = false)
    private Integer jobOrderMasterRefId;

    @Column(name = "ProblemName", nullable = false, length = 200)
    private String problemName;

    @Column(name = "ProductUse", length = 200)
    private String productUse;

    @Column(name = "ProductRefId")
    private Integer productRefId;

    @Column(name = "SupplierMasterRefId")
    private Integer supplierMasterRefId;

    @Column(name = "Cost", precision = 12, scale = 2)
    private BigDecimal cost;

    /** How many units this line consumed. The stock issue moves exactly this. */
    @Column(name = "Quantity", precision = 18, scale = 2)
    private BigDecimal quantity;

    /**
     * The stock OUT this line caused, null when the product is not carried by
     * the workshop store. Kept so removing the line during an edit can reverse
     * that exact movement rather than reconstruct it from today's figures.
     */
    @Column(name = "InventoryTransactionRefId")
    private Integer inventoryTransactionRefId;

    /**
     * The purchase order this line's part was bought on.
     *
     * Several lines share one PO when they were ordered from the same vendor,
     * which is why this is a plain reference and not a one-to-one.
     */
    @Column(name = "BillsOrderMasterRefId")
    private Integer billsOrderMasterRefId;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "Active", nullable = false)
    private Integer active = 1;

    @Column(name = "CreatedBy")
    private Integer createdBy;

    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "ModifiedBy")
    private Integer modifiedBy;

    @Column(name = "ModifiedDate")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JobOrderMasterRefId", insertable = false, updatable = false)
    private JobOrderMaster jobOrderMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SupplierMasterRefId", insertable = false, updatable = false)
    private my.maleva.api.module.supplier.entity.Supplier supplier;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (active == null) {
            active = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
