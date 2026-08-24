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

    /**
     * The workshop store item, when the line brings one in. Separate from
     * productCode/productName above, which carry the GL account this line posts
     * to - the account names the expense, these name the part.
     */
    private Integer inventoryProductRefId;
    private String storeItemCode;
    private String storeItemName;

    /**
     * When this line was received into stock, null until it has been. Carried
     * to the edit screen so a re-opened order shows "already received" from the
     * saved fact, not only from what happened earlier in the same browser tab.
     */
    private java.util.Date stockPushedDate;

    /** True once stockPushedDate is set - the flag the grid actually reads. */
    private Boolean stockPushed;
}

