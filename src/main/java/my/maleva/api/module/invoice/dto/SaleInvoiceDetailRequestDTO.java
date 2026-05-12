package my.maleva.api.module.invoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SaleInvoiceDetailRequestDTO
 * Request DTO for sale invoice line items/details
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
}

