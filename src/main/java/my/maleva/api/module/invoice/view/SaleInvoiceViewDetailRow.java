package my.maleva.api.module.invoice.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One invoice line on the view's expandable detail grid, legacy aliases kept. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleInvoiceViewDetailRow {

    @JsonProperty("SaleRefId")
    private Integer saleRefId;
    @JsonProperty("ProductCode")
    private String productCode;
    @JsonProperty("ProductName")
    private String productName;
    @JsonProperty("SDRemarks")
    private String sdRemarks;
    @JsonProperty("SaleRate")
    private Double saleRate;
    @JsonProperty("ItemQty")
    private Double itemQty;
    @JsonProperty("MRP")
    private Double mrp;
    @JsonProperty("TaxPercent")
    private Double taxPercent;
    @JsonProperty("TaxAmt")
    private Double taxAmt;
    @JsonProperty("DiscountPercent")
    private Double discountPercent;
    @JsonProperty("DiscountAmt")
    private Double discountAmt;
    @JsonProperty("SAmount")
    private Double sAmount;
    @JsonProperty("CurrencyValue")
    private Double currencyValue;
    @JsonProperty("ActualAmount")
    private Double actualAmount;
    @JsonProperty("SaleOrderMasterRefId")
    private Integer saleOrderMasterRefId;
    @JsonProperty("SaleOrderMasterNoDisplay")
    private String saleOrderMasterNoDisplay;
}
