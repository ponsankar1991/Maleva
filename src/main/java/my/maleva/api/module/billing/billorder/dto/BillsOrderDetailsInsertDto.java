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

    @NotNull(message = "Account Master Reference ID is required")
    @Min(value = 1, message = "Account Master Reference ID must be positive")
    private Integer accountMasterRefId;

    @DecimalMin(value = "0.0", message = "MRP must be positive")
    private Float mrp;

    @DecimalMin(value = "0.0", message = "Purchase Rate must be positive")
    private Float purchaseRate;

    @NotNull(message = "Item Quantity is required")
    @DecimalMin(value = "0.0", message = "Item Quantity must be positive")
    private Float itemQty;

    @DecimalMin(value = "0.0", message = "Discount Percentage must be positive")
    private Float discPer;

    @DecimalMin(value = "0.0", message = "Discount Amount must be positive")
    private Float discAmount;

    @DecimalMin(value = "0.0", message = "Landing Cost must be positive")
    private Float landingCost;

    @DecimalMin(value = "0.0", message = "Tax Percent must be positive")
    private Float taxPercent;

    @DecimalMin(value = "0.0", message = "Tax Amount must be positive")
    private Float taxAmount;

    @DecimalMin(value = "0.0", message = "Sales Rate must be positive")
    private Float salesRate;

    @DecimalMin(value = "0.0", message = "Net Sales Rate must be positive")
    private Float netSalesRate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private Float amount;

    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    private String remarksD;

    @DecimalMin(value = "0.0", message = "Currency Value must be positive")
    private Float currencyValue;

    @DecimalMin(value = "0.0", message = "Actual Amount must be positive")
    private Float actualAmount;

    private Integer productRefId;

    @DecimalMin(value = "0.0", message = "Quote Value must be positive")
    private Float quoteValue;

    @Size(max = 150, message = "Serial No cannot exceed 150 characters")
    private String serialNo;
}

