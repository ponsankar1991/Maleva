package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for DeletePurchaseMaster operation
 * Matches .NET JsonResult format: { ok: true/false, message: string }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletePurchaseMasterResponseDto {

    private boolean ok;
    private String message;
}
