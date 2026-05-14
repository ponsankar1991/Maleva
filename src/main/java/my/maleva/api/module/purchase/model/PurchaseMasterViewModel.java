package my.maleva.api.module.purchase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PurchaseMasterViewModel - Exact replica of .NET PurchaseMasterViewModel
 * Used for displaying purchase master information in views
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseMasterViewModel {

    private int id;
    private String billNoDisplay;
    private int billNo;
    private String billDate;
    private String invoiceNo;
    private String invoiceDate;
    private String billTime;
    private String saleType;
    private String supplierName;
    private String employeeName;
    private String cashierName;
    private String truckName;
    private String driverName;
    private String remarks;
    private float netAmt; // using float to match .NET Single
}
