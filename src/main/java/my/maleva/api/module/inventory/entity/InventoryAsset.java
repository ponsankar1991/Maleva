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

/**
 * One row per physical repairable unit or tool (a specific turbo, a specific diagnostic scanner).
 * ProductMaster describes the *kind* of item; InventoryAsset tracks *this one* through its life:
 * AVAILABLE (in store) -> INSTALLED (on a truck) -> UNDER_REPAIR -> AVAILABLE again -> ...
 */
@Entity
@Table(name = "InventoryAsset")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "ProductRefId", nullable = false)
    private Integer productRefId;

    @Column(name = "SerialNo", length = 100, nullable = false)
    private String serialNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20, nullable = false)
    private AssetStatus status;

    @Column(name = "CurrentTruckRefId")
    private Integer currentTruckRefId;

    /**
     * The truck this unit was last fitted to. currentTruckRefId is cleared the
     * moment a unit is removed, so without this the truck a core came off would
     * be lost as soon as it reached the recon shelf.
     */
    @Column(name = "LastTruckRefId")
    private Integer lastTruckRefId;

    /** NEW until the first completed recon, RECON from then on. */
    @Enumerated(EnumType.STRING)
    @Column(name = "Condition", length = 10, nullable = false)
    private AssetCondition condition;

    /** How many times this unit has been reconditioned. */
    @Column(name = "ReconCount", nullable = false)
    private Integer reconCount;

    /**
     * Cost basis of this individual unit: its purchase cost while NEW, then the
     * repair spend it re-entered stock at once RECON.
     */
    @Column(name = "CurrentValue", precision = 18, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductRefId", insertable = false, updatable = false)
    private ProductMaster productMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CurrentTruckRefId", insertable = false, updatable = false)
    private TruckMaster currentTruck;

    /**
     * Resolves LastTruckRefId to a name. A unit on the recon shelf has no
     * current truck, so this is the only way a screen can say where it came
     * from without the caller looking the truck up separately.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LastTruckRefId", insertable = false, updatable = false)
    private TruckMaster lastTruck;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (status == null) {
            status = AssetStatus.AVAILABLE;
        }
        // NOT NULL in the table, and a unit that has never been reconditioned
        // is by definition new with a recon count of zero.
        if (condition == null) {
            condition = AssetCondition.NEW;
        }
        if (reconCount == null) {
            reconCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
