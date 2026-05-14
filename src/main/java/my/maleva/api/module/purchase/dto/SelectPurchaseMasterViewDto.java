package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * View DTO for SelectPurchaseMaster operation response data
 * Wraps both master and detail records in a single response object
 * Equivalent to .NET PurchaseF5view model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectPurchaseMasterViewDto {

    private List<PurchaseMasterHistoryDto> purchaseMaster;

    private List<PurchaseDetailsHistoryDto> purchaseDetails;
}

