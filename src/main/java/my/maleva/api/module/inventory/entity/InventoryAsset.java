package my.maleva.api.module.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.productmaster.entity.ProductMaster;

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

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (status == null) {
            status = AssetStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
