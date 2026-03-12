package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SaleDetailsViewModel - Response DTO for sale order details
 * Maps to the .NET SaleDetailsViewModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDetailsViewModel {

    @JsonProperty("DiscountAmt")
    private Double discountAmt;

    @JsonProperty("DiscountPercent")
    private Double discountPercent;

    @JsonProperty("ItemQty")
    private Double itemQty;

    @JsonProperty("MRP")
    private Double mrp;

    @JsonProperty("ProductName")
    private String productName;

    @JsonProperty("SDRemarks")
    private String sdRemarks;

    @JsonProperty("SaleRate")
    private Double saleRate;

    @JsonProperty("SaleRefId")
    private Integer saleRefId;

    @JsonProperty("TaxAmt")
    private Double taxAmt;

    @JsonProperty("TaxPercent")
    private Double taxPercent;

    @JsonProperty("ProductCode")
    private String productCode;

    @JsonProperty("SAmount")
    private Double sAmount;

    @JsonProperty("CurrencyValue")
    private Double currencyValue;

    @JsonProperty("ActualAmount")
    private Double actualAmount;
}

