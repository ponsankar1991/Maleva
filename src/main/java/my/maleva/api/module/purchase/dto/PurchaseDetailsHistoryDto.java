package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PurchaseDetails History View
 * Represents a single row from the purchase details in the report view
 * Used in SelectPurchaseMaster response
 * Equivalent to .NET PurchaseDetailsViewModel model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDetailsHistoryDto {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("SaleRefId")
    private Integer saleRefId;

    @JsonProperty("PurchaseMasterRefId")
    private Integer purchaseMasterRefId;

    @JsonProperty("ProductCode")
    private String productCode;

    @JsonProperty("ProductName")
    private String productName;

    @JsonProperty("MRP")
    private Double mrp;

    @JsonProperty("SaleRate")
    private Double saleRate;

    @JsonProperty("TaxPercent")
    private Double taxPercent;

    @JsonProperty("TaxAmt")
    private Double taxAmount;

    @JsonProperty("DiscountPercent")
    private Double discountPercent;

    @JsonProperty("DiscountAmt")
    private Double discountAmount;

    @JsonProperty("ItemQty")
    private Double itemQty;

    @JsonProperty("SAmount")
    private Double amount;

    @JsonProperty("RemarksD")
    private String remarksD;
}

