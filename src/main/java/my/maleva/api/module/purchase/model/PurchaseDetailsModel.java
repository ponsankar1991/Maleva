package my.maleva.api.module.purchase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PurchaseDetailsModel - Exact replica of .NET PurchaseDetailsModel
 * Used for EditPurchaseMaster operation to maintain compatibility with .NET response structure
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDetailsModel {

    // Primary identifiers
    private int id; // (int, not null)
    private int sdId; // (int, not null)

    // Foreign keys
    private int purchaseMasterRefId; // (int, not null)
    private Integer productMasterRefId; // (int, nullable - changed to Integer to handle null values)

    // Pricing (using float to match .NET Single)
    private float mrp; // (real, not null)
    private float purchaseRate; // (real, not null)
    private float itemQty; // (real, not null)
    private float discPer; // (real, not null)
    private float discAmount; // (real, not null)
    private float landingCost; // (real, not null)
    private float taxPercent; // (real, not null)
    private float taxAmount; // (real, not null)
    private float salesRate; // (real, not null)
    private float netSalesRate; // (real, not null)
    private float amount; // (real, not null)

    // Currency values
    private float currencyValue; // (real, not null)
    private float actualAmount; // (real, not null)

    // Remarks and product info
    private String remarksD; // (varchar, nullable)
    private String productCode; // (varchar, nullable)
    private String productName; // (varchar, nullable)
    private String uom; // (varchar, nullable)
}
