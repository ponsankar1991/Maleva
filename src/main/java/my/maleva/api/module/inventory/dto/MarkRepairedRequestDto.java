package my.maleva.api.module.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkRepairedRequestDto {

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotNull(message = "Product Reference ID is required")
    private Integer productRefId;

    @NotBlank(message = "Serial No is required")
    @Size(max = 100, message = "Serial No cannot exceed 100 characters")
    private String serialNo;

    @Size(max = 200, message = "Remarks cannot exceed 200 characters")
    private String remarks;

    @NotBlank(message = "Created By is required")
    @Size(max = 50, message = "Created By cannot exceed 50 characters")
    private String createdBy;
}
