package my.maleva.api.module.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Create / update payload for a workshop inventory item.
 * On create, openingQty (quantity items) or firstSerialNo (serialised items)
 * seeds the opening balance. Both are ignored on update - stock only ever
 * changes through a recorded movement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemRequestDto {

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    /**
     * Create only. Set it to bring a product that already exists in ProductMaster
     * into the workshop store - its code and name are then taken from that record
     * and itemCode / itemName here are ignored, so one physical thing never ends
     * up as two products. Leave it null to create a brand new product.
     */
    private Integer productRefId;

    @NotBlank(message = "Item Code is required")
    @Size(max = 50, message = "Item Code cannot exceed 50 characters")
    private String itemCode;

    @NotBlank(message = "Item Name is required")
    @Size(max = 100, message = "Item Name cannot exceed 100 characters")
    private String itemName;

    /** CONSUMABLE, PART, ASSET or TOOL. */
    @NotBlank(message = "Item Type is required")
    private String itemType;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;

    @Size(max = 200, message = "Fits Model cannot exceed 200 characters")
    private String fitsModel;

    @Size(max = 20, message = "Base UOM cannot exceed 20 characters")
    private String baseUom;

    @DecimalMin(value = "0", message = "Minimum Quantity must be 0 or greater")
    private Double minQty;

    @DecimalMin(value = "0", message = "Reorder Quantity must be 0 or greater")
    private Double reorderQty;

    @DecimalMin(value = "0", message = "Unit Cost must be 0 or greater")
    private Double unitCost;

    @Size(max = 100, message = "Storage Location cannot exceed 100 characters")
    private String storageLocation;

    @Size(max = 50, message = "Bin Code cannot exceed 50 characters")
    private String binCode;

    private Integer defaultSupplierRefId;

    @Size(max = 200, message = "Remarks cannot exceed 200 characters")
    private String remarks;

    /** Opening balance for quantity-tracked items. Create only. */
    @DecimalMin(value = "0", message = "Opening Quantity must be 0 or greater")
    private BigDecimal openingQty;

    /** First physical unit for serialised items. Create only. */
    @Size(max = 100, message = "Serial No cannot exceed 100 characters")
    private String firstSerialNo;

    /** Accounting links on ProductMaster. Resolved to the company default when omitted. */
    private Integer taxCode;
    private Integer uomCode;

    @NotBlank(message = "Modified By is required")
    @Size(max = 50, message = "Modified By cannot exceed 50 characters")
    private String modifiedBy;
}
