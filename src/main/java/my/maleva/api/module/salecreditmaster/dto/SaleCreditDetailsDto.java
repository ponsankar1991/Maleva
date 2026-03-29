package my.maleva.api.module.salecreditmaster.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleCreditDetailsDto
 * Data Transfer Object for SaleCreditDetails API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleCreditDetailsDto {

    private Integer id;

    @NotNull(message = "Sale Credit Master Reference ID is required")
    private Integer saleCreditMasterRefId;

    @NotNull(message = "Item Master Reference ID is required")
    private Integer itemMasterRefId;

    @NotNull(message = "MRP is required")
    @Min(value = 0, message = "MRP must be greater than or equal to 0")
    private Double mrp;

    @NotNull(message = "Purchase Rate is required")
    @Min(value = 0, message = "Purchase Rate must be greater than or equal to 0")
    private Double purchaseRate;

    @NotNull(message = "Item Quantity is required")
    @Min(value = 0, message = "Item Quantity must be greater than or equal to 0")
    private Double itemQty;

    @NotNull(message = "Discount Percentage is required")
    @Min(value = 0, message = "Discount Percentage must be greater than or equal to 0")
    private Double discPer;

    @NotNull(message = "Discount Amount is required")
    @Min(value = 0, message = "Discount Amount must be greater than or equal to 0")
    private Double discAmount;

    @NotNull(message = "Landing Cost is required")
    @Min(value = 0, message = "Landing Cost must be greater than or equal to 0")
    private Double landingCost;

    @NotNull(message = "Tax Percent is required")
    @Min(value = 0, message = "Tax Percent must be greater than or equal to 0")
    private Double taxPercent;

    @NotNull(message = "Tax Amount is required")
    @Min(value = 0, message = "Tax Amount must be greater than or equal to 0")
    private Double taxAmount;

    @NotNull(message = "Sales Rate is required")
    @Min(value = 0, message = "Sales Rate must be greater than or equal to 0")
    private Double salesRate;

    @NotNull(message = "Net Sales Rate is required")
    @Min(value = 0, message = "Net Sales Rate must be greater than or equal to 0")
    private Double netSalesRate;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    private Double amount;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private Double currencyValue;

    private Double actualAmount;
}

