package my.maleva.api.module.purchase.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for EditPurchaseOrderMaster operation
 * Contains parameters needed to fetch and edit a purchase order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditPurchaseOrderMasterRequestDto {

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Integer companyId;

    /**
     * Purchase order ID - used when available
     */
    private Integer id;

    /**
     * Purchase order master number (CNumber) - used to lookup ID if id is 0
     * If provided and not 0, it will be used to find the actual ID
     */
    private Integer purchaseOrderMasterNo;
}

