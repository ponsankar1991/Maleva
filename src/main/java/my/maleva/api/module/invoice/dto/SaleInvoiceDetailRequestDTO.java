package my.maleva.api.module.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One line of a sale invoice, as {@code SP_SaleMaster} reads it.
 *
 * <p>The procedure re-reads every line from the payload on each save: an edit
 * deletes the existing SaleDetails rows and inserts these, so a line omitted
 * here is a line deleted. Field names map to the {@code OPENJSON ... WITH}
 * block of the procedure's SaleDetails insert, not to the SaleDetails table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoiceDetailRequestDTO {

    @NotNull(message = "Item Master Reference ID is required")
    @Positive(message = "Item Master Reference ID must be positive")
    private Integer itemMasterRefId;

    private Integer taxRefId;

    @NotNull(message = "Item Quantity is required")
    @Positive(message = "Item Quantity must be positive")
    private Double itemQty;

    @NotNull(message = "Sales Rate is required")
    private Double salesRate;

    private Double discountPercent;

    private Double discountAmount;

    private Double landingCost;

    private Double taxPercent;

    private Double taxAmount;

    private Double amount;

    private String productCode;

    private String productName;

    private String remarks;

    private String uom;

    private Double currencyValue;

    /** Written to SaleDetails.MRP. The screen always sends 0. */
    private Double mrp;

    /** Written to SaleDetails.PurchaseRate. The screen always sends 0. */
    private Double purchaseRate;

    /** Written to SaleDetails.NetSalesRate. The screen always sends 0. */
    private Double netSalesRate;

    /** Amount in the customer's currency: {@code amount * currencyValue}. */
    private Double actualAmount;

    /**
     * The sale order this line came from. Drives the "- JobNo" remark suffix on
     * multi-job invoices and, through {@code saleOrderRefIds} on the request,
     * the SaleMasterReference rows.
     */
    private Integer saleOrderMasterRefId;
}
