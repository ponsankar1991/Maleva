package my.maleva.api.module.inventory.recon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Finish a repair and put the unit back on the shelf as recon stock. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteReconRequestDto {

    private LocalDateTime receivedDate;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @NotBlank(message = "Modified By is required")
    @Size(max = 50, message = "Modified By cannot exceed 50 characters")
    private String modifiedBy;
}
