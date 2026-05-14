package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for MaxPurchaseMasterNo operation
 * Matches .NET JsonResult format: { ok: true/false, No: string, message: string }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaxPurchaseMasterNoResponseDto {

    private boolean ok;
    private String no;
    private String message;
}
