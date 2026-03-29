package my.maleva.api.module.saleorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleOrderDetailsDto - DTO for SaleOrderDetails
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderDetailsDto {
    private Integer id;
    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;
    @NotNull(message = "Item Master Reference ID is required")
    private Integer itemMasterRefId;
    @NotNull(message = "MRP is required")
    @Min(value = 0)
    private Double mrp;
    @NotNull(message = "Purchase Rate is required")
    @Min(value = 0)
    private Double purchaseRate;
    @NotNull(message = "Item Quantity is required")
    @Min(value = 0)
    private Double itemQty;
    @NotNull(message = "Discount Percentage is required")
    @Min(value = 0)
    private Double discPer;
    @NotNull(message = "Discount Amount is required")
    @Min(value = 0)
    private Double discAmount;
    @NotNull(message = "Landing Cost is required")
    @Min(value = 0)
    private Double landingCost;
    @NotNull(message = "Tax Percent is required")
    @Min(value = 0)
    private Double taxPercent;
    @NotNull(message = "Tax Amount is required")
    @Min(value = 0)
    private Double taxAmount;
    @NotNull(message = "Sales Rate is required")
    @Min(value = 0)
    private Double salesRate;
    @NotNull(message = "Net Sales Rate is required")
    @Min(value = 0)
    private Double netSalesRate;
    @NotNull(message = "Amount is required")
    @Min(value = 0)
    private Double amount;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Double currencyValue;
    private Double actualAmount;
    @Size(max = 300)
    private String sdRemarks;
    private Integer taxRefId;
}

