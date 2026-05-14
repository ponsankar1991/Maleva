package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for InsertPurchaseMaster operation
 * Matches .NET JsonResult format: { ok: true/false, message: string, name: string, id: integer }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsertPurchaseMasterResponseDto {

    private boolean ok;
    private String message;
    private String name;
    private Integer id;
}
