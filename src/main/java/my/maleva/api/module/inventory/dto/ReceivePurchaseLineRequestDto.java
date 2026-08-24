package my.maleva.api.module.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One purchase order line being received into the workshop store.
 *
 * Kept separate from StockInRequestDto because the two are asked different
 * questions. A stock-in assumes the store already carries the product and only
 * moves a balance. A goods receipt cannot assume that: the first delivery of a
 * part arrives before anyone has catalogued it, so this call creates the store
 * record when it is missing and then records the movement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceivePurchaseLineRequestDto {

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    /** ProductMaster.Id taken from the order line's store product. */
    @NotNull(message = "Product Reference ID is required")
    private Integer productRefId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    /**
     * Price of one unit from the order line.
     *
     * Optional so a receipt is still possible when the rate has not been agreed,
     * but a value here is what gives the movement its worth.
     */
    @DecimalMin(value = "0", message = "Unit cost cannot be negative")
    private BigDecimal unitCost;

    /**
     * Kind of stock this is: CONSUMABLE, PART, ASSET or TOOL.
     *
     * Used only when the store does not carry the product yet - an item already
     * catalogued keeps the type it was given, because a goods receipt is not the
     * place to reclassify it. Defaults to PART when the caller says nothing.
     */
    @Size(max = 20, message = "Item Type cannot exceed 20 characters")
    private String itemType;

    /**
     * Supplier.Id the order was raised against.
     *
     * Used only when the store does not carry the product yet, to set the item's
     * default supplier - the same reasoning as itemType above: an item already
     * catalogued keeps the supplier it was given, so re-ordering from a different
     * vendor on one occasion does not silently change who the store re-orders
     * from by default.
     */
    private Integer supplierRefId;

    /** BillsOrderMaster.Id, so the receipt can be traced back to its order. */
    private Integer purchaseOrderRefId;

    /**
     * BillsOrderDetails.Id of the specific line being received.
     *
     * Required, not merely traceable like purchaseOrderRefId above: this is what
     * lets the server tell "receive line 41" apart from "receive line 42 for the
     * same product on the same order", and it is what a second click on the same
     * line is checked against. A line with no saved id - one just added to the
     * grid and not yet saved - cannot be received, because there would be
     * nothing to mark and a second click could not be told from the first.
     */
    @NotNull(message = "Order line reference is required - save the order before receiving a line")
    private Integer billsOrderDetailsRefId;

    @Size(max = 200, message = "Remarks cannot exceed 200 characters")
    private String remarks;

    @NotBlank(message = "Created By is required")
    @Size(max = 50, message = "Created By cannot exceed 50 characters")
    private String createdBy;
}
