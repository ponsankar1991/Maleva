package my.maleva.api.module.inventory.recon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Write off a unit that could not be repaired. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapReconRequestDto {

    @NotBlank(message = "A reason is required to scrap a unit")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @NotBlank(message = "Modified By is required")
    @Size(max = 50, message = "Modified By cannot exceed 50 characters")
    private String modifiedBy;
}
