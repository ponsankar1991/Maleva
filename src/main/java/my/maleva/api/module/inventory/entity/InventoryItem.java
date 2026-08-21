package my.maleva.api.module.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.maleva.api.module.productmaster.entity.ProductMaster;
import my.maleva.api.module.supplier.entity.Supplier;

import java.time.LocalDateTime;

/**
 * Workshop inventory configuration for a product, one row per company per product.
 *
 * ProductMaster stays the sales/accounting record (code, name, tax, UOM, rates) and is
 * left untouched; everything the workshop store needs that ProductMaster has no column
 * for lives here. Current balance lives in ProductMasterCStock, movements in
 * InventoryTransaction, individual serialised units in InventoryAsset.
 *
 * Note on units: ProductMaster.uomCode is the accounting UOM foreign key, while
 * baseUom here is the stock-keeping label shown on store screens ("L", "Pcs", "Unit"),
 * kept denormalised so list screens need no UOM master join.
 */
@Entity
@Table(name = "InventoryItem")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "ProductRefId", nullable = false)
    private Integer productRefId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ItemType", length = 20, nullable = false)
    private ItemType itemType;

    @Column(name = "Category", length = 100)
    private String category;

    @Column(name = "Brand", length = 100)
    private String brand;

    @Column(name = "FitsModel", length = 200)
    private String fitsModel;

    @Column(name = "BaseUom", length = 20, nullable = false)
    private String baseUom;

    @Column(name = "MinQty")
    private Double minQty;

    @Column(name = "ReorderQty")
    private Double reorderQty;

    @Column(name = "UnitCost")
    private Double unitCost;

    @Column(name = "StorageLocation", length = 100)
    private String storageLocation;

    @Column(name = "BinCode", length = 50)
    private String binCode;

    @Column(name = "DefaultSupplierRefId")
    private Integer defaultSupplierRefId;

    @Column(name = "Remarks", length = 200)
    private String remarks;

    @Column(name = "Active", nullable = false)
    private Integer active;

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
    @JoinColumn(name = "DefaultSupplierRefId", insertable = false, updatable = false)
    private Supplier defaultSupplier;

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
