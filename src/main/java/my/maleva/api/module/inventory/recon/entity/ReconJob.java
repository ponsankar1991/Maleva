package my.maleva.api.module.inventory.recon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.inventory.entity.InventoryAsset;
import my.maleva.api.module.productmaster.entity.ProductMaster;
import my.maleva.api.module.supplier.entity.Supplier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One unit removed from a truck, and everything that happened to it after.
 *
 * InventoryAsset only records where a unit is now. When a unit comes off a
 * truck its currentTruckRefId is cleared, so the asset row alone cannot answer
 * "where did this come from", "who repaired it" or "what did that cost". This
 * is the document that does.
 */
@Entity
@Table(name = "InventoryReconJob")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    /** Human-facing document number, e.g. RCN000000014. */
    @Column(name = "ReconNo", length = 30, nullable = false)
    private String reconNo;

    // ------------------------------------------------------------- removed

    @Column(name = "ProductRefId", nullable = false)
    private Integer productRefId;

    @Column(name = "AssetRefId", nullable = false)
    private Integer assetRefId;

    @Column(name = "SerialNo", length = 100, nullable = false)
    private String serialNo;

    /** The truck the unit was taken off. The reason this table exists. */
    @Column(name = "RemovedFromTruckRefId", nullable = false)
    private Integer removedFromTruckRefId;

    @Column(name = "RemovedOnJobOrderRefId")
    private Integer removedOnJobOrderRefId;

    @Column(name = "RemovedDate", nullable = false)
    private LocalDateTime removedDate;

    @Column(name = "RemovedBy", length = 50, nullable = false)
    private String removedBy;

    @Column(name = "FaultDescription", length = 500)
    private String faultDescription;

    // ------------------------------------------------------------ replaced

    /**
     * The unit fitted in its place. Kept so the pair can be read from either
     * end: what went into the truck, and what came out of it.
     */
    @Column(name = "ReplacedByProductRefId")
    private Integer replacedByProductRefId;

    @Column(name = "ReplacedBySerialNo", length = 100)
    private String replacedBySerialNo;

    /** Whether the replacement was itself new or a recon unit off the shelf. */
    @Column(name = "ReplacedByCondition", length = 10)
    private String replacedByCondition;

    // -------------------------------------------------------------- repair

    @Enumerated(EnumType.STRING)
    @Column(name = "RepairMode", length = 10)
    private RepairMode repairMode;

    /** Set only for RepairMode.VENDOR - see CK_Recon_Vendor on the table. */
    @Column(name = "VendorRefId")
    private Integer vendorRefId;

    @Column(name = "VendorDocNo", length = 50)
    private String vendorDocNo;

    @Column(name = "SentDate")
    private LocalDateTime sentDate;

    @Column(name = "ExpectedDate")
    private LocalDateTime expectedDate;

    @Column(name = "ReceivedDate")
    private LocalDateTime receivedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 15, nullable = false)
    private ReconStatus status;

    // ---------------------------------------------------------------- cost

    /**
     * Rolled up from InventoryReconCost by the service on every line change.
     * Denormalised so the list and summary screens do not have to aggregate
     * the child table for every row.
     */
    @Column(name = "LabourCost", precision = 18, scale = 2, nullable = false)
    private BigDecimal labourCost;

    @Column(name = "PartsCost", precision = 18, scale = 2, nullable = false)
    private BigDecimal partsCost;

    @Column(name = "VendorCost", precision = 18, scale = 2, nullable = false)
    private BigDecimal vendorCost;

    @Column(name = "OtherCost", precision = 18, scale = 2, nullable = false)
    private BigDecimal otherCost;

    /** Computed column in the table - never written from here. */
    @Column(name = "TotalCost", precision = 18, scale = 2, insertable = false, updatable = false)
    private BigDecimal totalCost;

    /**
     * What the unit is worth when it goes back on the shelf. The removed core
     * is carried at zero, having been expensed when it was first fitted, so
     * this is the repair spend - which is what makes a recon unit and a new
     * one directly comparable on a report.
     */
    @Column(name = "ResultingUnitCost", precision = 18, scale = 2)
    private BigDecimal resultingUnitCost;

    /** What a new one would have cost, for the savings figure. */
    @Column(name = "NewPartCost", precision = 18, scale = 2)
    private BigDecimal newPartCost;

    // --------------------------------------------------------------- audit

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    // ---------------------------------------------------------- read-only

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductRefId", insertable = false, updatable = false)
    private ProductMaster productMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssetRefId", insertable = false, updatable = false)
    private InventoryAsset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RemovedFromTruckRefId", insertable = false, updatable = false)
    private TruckMaster removedFromTruck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VendorRefId", insertable = false, updatable = false)
    private Supplier vendor;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        modifiedDate = now;
        if (removedDate == null) {
            removedDate = now;
        }
        if (status == null) {
            status = ReconStatus.PENDING;
        }
        if (active == null) {
            active = 1;
        }
        // All four are NOT NULL and feed the computed TotalCost column, so a
        // job with no cost lines yet must still total to zero rather than null.
        labourCost = zeroIfNull(labourCost);
        partsCost = zeroIfNull(partsCost);
        vendorCost = zeroIfNull(vendorCost);
        otherCost = zeroIfNull(otherCost);
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
        labourCost = zeroIfNull(labourCost);
        partsCost = zeroIfNull(partsCost);
        vendorCost = zeroIfNull(vendorCost);
        otherCost = zeroIfNull(otherCost);
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
