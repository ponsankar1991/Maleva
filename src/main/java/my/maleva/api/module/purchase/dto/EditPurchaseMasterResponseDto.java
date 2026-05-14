package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.maleva.api.module.purchase.model.PurchaseMasterModel;
import java.util.List;

/**
 * Response DTO for EditPurchaseMaster operation
 * Returns the full purchase master record with all details
 * Equivalent to .NET ResponseViewModel with PurchaseMasterModel data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditPurchaseMasterResponseDto {

    @Builder.Default
    private boolean ok = true;

    private String message;

    /**
     * List containing single PurchaseMasterModel with all details populated
     * Wraps the result to maintain consistent response format
     * Uses PurchaseMasterModel to match exact .NET structure
     */
    private List<PurchaseMasterModel> data;
}
