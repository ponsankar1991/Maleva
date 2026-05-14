package my.maleva.api.module.purchase.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for EditPurchaseMaster operation
 * Contains parameters needed to fetch and edit a purchase master record
 *
 * Equivalent to .NET EditPurchaseMaster method parameters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditPurchaseMasterRequestDto {

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Integer companyId;

    /**
     * Purchase master ID - used when available
     * If this is 0 or null, purchaseMasterNo will be used for lookup
     */
    private Integer id;

    /**
     * Purchase master number (CNumber) - used to lookup ID if id is 0
     * If provided and not 0, it will be used to find the actual ID
     * Equivalent to .NET PurchaseMasterNo parameter
     */
    private Integer purchaseMasterNo;
}

