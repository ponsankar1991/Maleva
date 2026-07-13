package my.maleva.api.module.saleorder.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating the DODescription of a SaleOrder
 *
 * Validation Rules:
 * - id: REQUIRED, MUST BE POSITIVE (> 0)
 * - doDescription: REQUIRED, NOT BLANK, MAX 500 chars
 */
@Data
public class SaleOrderDoDescriptionUpdateDto {
    
    @NotNull(message = "SaleOrder ID is required")
    @Positive(message = "SaleOrder ID must be a positive number (greater than 0)")
    private Integer id;

    @NotBlank(message = "DODescription is required and cannot be blank")
    @Size(min = 1, max = 500, message = "DODescription must be between 1 and 500 characters")
    private String doDescription;
}
