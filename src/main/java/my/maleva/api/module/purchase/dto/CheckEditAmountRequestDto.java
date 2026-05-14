package my.maleva.api.module.purchase.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for CheckEditAmount operation
 * Contains the parameters needed to calculate total payment amount for a purchase order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckEditAmountRequestDto {

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Integer companyId;

    @NotNull(message = "Purchase ID is required")
    @Positive(message = "Purchase ID must be positive")
    private Integer purchaseId;
}
