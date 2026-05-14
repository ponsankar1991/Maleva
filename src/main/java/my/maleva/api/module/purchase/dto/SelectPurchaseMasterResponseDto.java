package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Response DTO for SelectPurchaseMaster operation
 * Wraps the combined master and detail records with status information
 * Equivalent to ResponseViewModel from .NET implementation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectPurchaseMasterResponseDto {

    @Builder.Default
    private boolean ok = true;

    private String message;

    private List<SelectPurchaseMasterViewDto> data;
}

