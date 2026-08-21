package my.maleva.api.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Full detail view of one inventory item: catalogue fields, current balance,
 * and - for serialised items - how its physical units are distributed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemResponseDto {

    private Integer id;
    private Integer companyRefId;
    private Integer productRefId;
    private String itemCode;
    private String itemName;
    private String itemType;
    private boolean serialised;
    private String category;
    private String brand;
    private String fitsModel;
    private String baseUom;
    private Double minQty;
    private Double reorderQty;
    private Double unitCost;
    private String storageLocation;
    private String binCode;
    private Integer defaultSupplierRefId;
    private String defaultSupplierName;
    private String remarks;
    private Integer active;

    /** Quantity on hand for quantity items; count of AVAILABLE units for serialised items. */
    private BigDecimal onHand;
    private BigDecimal stockValue;
    private String stockStatus;

    private Integer totalUnits;
    private Integer availableUnits;
    private Integer installedUnits;
    private Integer underRepairUnits;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
}
