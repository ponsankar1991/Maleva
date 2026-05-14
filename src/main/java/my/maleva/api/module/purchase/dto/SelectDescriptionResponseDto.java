package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for SelectDescription operation
 * Matches .NET JsonResult format: { ok: true/false, data: list, message: string }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectDescriptionResponseDto {

    private boolean ok;
    private List<String> data;
    private String message;
}
