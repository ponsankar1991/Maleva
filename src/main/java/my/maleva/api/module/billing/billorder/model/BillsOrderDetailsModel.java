package my.maleva.api.module.billing.billorder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderDetailsModel {
    private Integer id;
    private Integer SDId;
    private Integer billsOrderMasterRefId;
    private Integer accountMasterRefId;
    private Integer productRefId;
    private Float MRP;
    private Float quoteValue;
    private Float purchaseRate;
    private Float itemQty;
    private Float discPer;
    private Float discAmount;
    private Float landingCost;
    private Float taxPercent;
    private Float taxAmount;
    private Float salesRate;
    private Float netSalesRate;
    private Float amount;
    private String remarksD;
    private String serialNo;
    private Float currencyValue;
    private Float actualAmount;
    private String productCode;
    private String productName;
    private String uom;
}

