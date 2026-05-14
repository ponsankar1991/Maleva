package my.maleva.api.module.purchase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PurchaseDetailsViewModel - Exact replica of .NET PurchaseDetailsViewModel
 * Used for displaying purchase details information in views
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDetailsViewModel {

    private int id;
    private int saleRefId;
    private int purchaseMasterRefId;
    private String productCode;
    private String productName;
    private float mrp; // using float to match .NET Single
    private float saleRate;
    private float taxPercent;
    private float taxAmt;
    private float discountPercent;
    private float discountAmt;
    private float itemQty;
    private float sAmount;
    private String remarksD;
}
