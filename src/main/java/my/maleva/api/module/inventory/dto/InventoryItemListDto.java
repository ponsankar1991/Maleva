package my.maleva.api.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One row of the stock list screen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemListDto {

    private Integer id;
    private Integer productRefId;
    private String itemCode;
    private String itemName;
    private String itemType;
    private boolean serialised;
    private String category;
    private String baseUom;
    private BigDecimal onHand;
    private Double minQty;
    private Double unitCost;
    private BigDecimal stockValue;
    private String storageLocation;

    /** IN_STOCK, LOW_STOCK or OUT_OF_STOCK. */
    private String stockStatus;

    private Integer totalUnits;
}
