package my.maleva.api.module.purchase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SparePartsReportView - Exact replica of .NET SparePartsReportView
 * Used for spare parts reporting functionality
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartsReportView {

    private String employeeName;
    private String billDate;
    private String billTime;
    private String serialNo;
    private String invoiceNo;
    private String invoiceDate;
    private String billNoDisplay;
    private String supplierName;
    private float netAmt; // (int, not null) - using float to match .NET Single
    private float salesRate; // (int, not null)
    private float amount; // (int, not null)
    private String saleType;
    private String truckName;
    private String driverName;
    private String remarksD;
    private String productCode;
    private String productName;
    private int itemQty; // (int, not null)
}
