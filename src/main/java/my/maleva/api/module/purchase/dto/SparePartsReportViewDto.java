package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for SparePartsReportView
 * Represents a single row from the spare parts report view
 * Equivalent to .NET SparePartsReportView model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartsReportViewDto {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("EmployeeName")
    private String employeeName;

    @JsonProperty("BillDate")
    private String billDate;

    @JsonProperty("BillTime")
    private String billTime;

    @JsonProperty("SerialNo")
    private String serialNo;

    @JsonProperty("InvoiceNo")
    private String invoiceNo;

    @JsonProperty("InvoiceDate")
    private String invoiceDate;

    @JsonProperty("BillNoDisplay")
    private String billNoDisplay;

    @JsonProperty("SupplierName")
    private String supplierName;

    @JsonProperty("NetAmt")
    private Double netAmt;

    @JsonProperty("SalesRate")
    private Double salesRate;

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("SaleType")
    private String saleType;

    @JsonProperty("TruckName")
    private String truckName;

    @JsonProperty("DriverName")
    private String driverName;

    @JsonProperty("RemarksD")
    private String remarksD;

    @JsonProperty("ProductCode")
    private String productCode;

    @JsonProperty("ProductName")
    private String productName;

    @JsonProperty("ItemQty")
    private Integer itemQty;
}

