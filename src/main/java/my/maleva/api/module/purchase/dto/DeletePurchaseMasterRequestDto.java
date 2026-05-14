package my.maleva.api.module.purchase.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for DeletePurchaseMaster operation
 * Contains parameters needed to delete (soft delete with Active flag = 2) a purchase master record
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletePurchaseMasterRequestDto {

    @NotNull(message = "Purchase Master ID is required")
    @Positive(message = "Purchase Master ID must be positive")
    private Integer id;

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Integer companyId;
}

