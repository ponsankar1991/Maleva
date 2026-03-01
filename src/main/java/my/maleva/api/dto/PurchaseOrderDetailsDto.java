package my.maleva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * PurchaseOrderDetailsDto
 * Data Transfer Object for PurchaseOrderDetails API layer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDetailsDto {

    private Integer id;

    @NotNull(message = "Purchase Order Master Reference ID is required")
    private Integer purchaseOrderMasterRefId;

    @NotNull(message = "Product Master Reference ID is required")
    private Integer productMasterRefId;

    @Min(value = 0, message = "MRP must be 0 or greater")
    private Double mrp;

    @Min(value = 0, message = "Purchase Rate must be 0 or greater")
    private Double purchaseRate;

    @Min(value = 0, message = "Item Quantity must be 0 or greater")
    private Double itemQty;

    @Min(value = 0, message = "Discount Percentage must be 0 or greater")
    @Max(value = 100, message = "Discount Percentage cannot exceed 100")
    private Double discPer;

    @Min(value = 0, message = "Discount Amount must be 0 or greater")
    private Double discAmount;

    @Min(value = 0, message = "Landing Cost must be 0 or greater")
    private Double landingCost;

    @Min(value = 0, message = "Tax Percent must be 0 or greater")
    private Double taxPercent;

    @Min(value = 0, message = "Tax Amount must be 0 or greater")
    private Double taxAmount;

    @Min(value = 0, message = "Sales Rate must be 0 or greater")
    private Double salesRate;

    @Min(value = 0, message = "Net Sales Rate must be 0 or greater")
    private Double netSalesRate;

    @Min(value = 0, message = "Amount must be 0 or greater")
    private Double amount;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    private String remarksD;

    @Min(value = 0, message = "Currency Value must be 0 or greater")
    private Double currencyValue;

    @Min(value = 0, message = "Actual Amount must be 0 or greater")
    private Double actualAmount;
}

