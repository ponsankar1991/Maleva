package my.maleva.api.module.purchase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * PurchaseF5view - Exact replica of .NET PurchaseF5view
 * Container for combined PurchaseMaster and PurchaseDetails data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseF5view {

    private List<PurchaseMasterViewModel> purchaseMaster;
    private List<PurchaseDetailsViewModel> purchaseDetails;
}
