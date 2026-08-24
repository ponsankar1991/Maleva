package my.maleva.api.module.billing.billorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * DTO for BillsOrderDetails within insert request
 * Equivalent to .NET BillsOrderDetailsModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderDetailsInsertDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id = 0;

    private String productCode = "";

    private String productName = "";

    @NotNull(message = "Account Master Reference ID is required")
    @Min(value = 1, message = "Account Master Reference ID must be positive")
    private Integer accountMasterRefId;

    @DecimalMin(value = "0.0", message = "MRP must be positive")
    private Float mrp = 0.0f;

    @DecimalMin(value = "0.0", message = "Purchase Rate must be positive")
    private Float purchaseRate = 0.0f;

    @NotNull(message = "Item Quantity is required")
    @DecimalMin(value = "0.0", message = "Item Quantity must be positive")
    private Float itemQty = 0.0f;

    @DecimalMin(value = "0.0", message = "Discount Percentage must be positive")
    private Float discPer = 0.0f;

    @DecimalMin(value = "0.0", message = "Discount Amount must be positive")
    private Float discAmount = 0.0f;

    @DecimalMin(value = "0.0", message = "Landing Cost must be positive")
    private Float landingCost = 0.0f;

    @DecimalMin(value = "0.0", message = "Tax Percent must be positive")
    private Float taxPercent = 0.0f;

    @DecimalMin(value = "0.0", message = "Tax Amount must be positive")
    private Float taxAmount = 0.0f;

    @DecimalMin(value = "0.0", message = "Sales Rate must be positive")
    private Float salesRate = 0.0f;

    @DecimalMin(value = "0.0", message = "Net Sales Rate must be positive")
    private Float netSalesRate = 0.0f;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private Float amount = 0.0f;

    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    private String remarksD = "";

    @DecimalMin(value = "0.0", message = "Currency Value must be positive")
    private Float currencyValue = 0.0f;

    @DecimalMin(value = "0.0", message = "Actual Amount must be positive")
    private Float actualAmount = 0.0f;

    private Integer productRefId = 0;

    @DecimalMin(value = "0.0", message = "Quote Value must be positive")
    private Float quoteValue = 0.0f;

    @Size(max = 150, message = "Serial No cannot exceed 150 characters")
    private String serialNo = "";

    private Integer billsOrderMasterRefId = 0;

    private Integer editMode = 1;

    /**
     * ProductMaster id of the store item this line brings in, when it is one.
     *
     * Left null by service lines, so it defaults to null rather than 0 - a zero
     * would look like a real product to the stock-in loop.
     */
    private Integer inventoryProductRefId;
}

