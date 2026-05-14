package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for PurchaseMaster History View
 * Represents a single row from the purchase master report view
 * Used in SelectPurchaseMaster response
 * Equivalent to .NET PurchaseMasterViewModel model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseMasterHistoryDto {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("BillNoDisplay")
    private String billNoDisplay;

    @JsonProperty("BillNo")
    private Integer billNo;

    @JsonProperty("BillDate")
    private String billDate;

    @JsonProperty("InvoiceNo")
    private String invoiceNo;

    @JsonProperty("InvoiceDate")
    private String invoiceDate;

    @JsonProperty("BillTime")
    private String billTime;

    @JsonProperty("SaleType")
    private String saleType;

    @JsonProperty("SupplierName")
    private String supplierName;

    @JsonProperty("EmployeeName")
    private String employeeName;

    @JsonProperty("CashierName")
    private String cashierName;

    @JsonProperty("TruckName")
    private String truckName;

    @JsonProperty("DriverName")
    private String driverName;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("NetAmt")
    private Double netAmount;
}

