package my.maleva.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SaleDetailDTO - DTO for individual sale order detail lines
 * Represents line items in a sale order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDetailDTO {

    private Integer id;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    private Integer productRefId;

    @Size(max = 100)
    private String productCode;

    @Size(max = 500)
    private String description;

    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer qty;

    @Min(value = 0, message = "Rate must be greater than or equal to 0")
    private Double rate;

    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    private Double amount;

    @Min(value = 0, message = "Tax Percentage must be greater than or equal to 0")
    private Double taxPercentage;

    @Min(value = 0, message = "GST Amount must be greater than or equal to 0")
    private Double gstAmount;

    @Min(value = 0, message = "Total Amount must be greater than or equal to 0")
    private Double totalAmount;

    private Integer rowNumber;

    private Integer editMode;
}

