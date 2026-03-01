package my.maleva.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TaxMasterDto - DTO for TaxMaster
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotBlank(message = "Tax Code is required")
    @Size(max = 50, message = "Tax Code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    @NotNull(message = "Tax rate is required")
    private Float tax;

    @NotNull(message = "Tax IO is required")
    private Integer taxIO;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    @NotNull(message = "Active status is required")
    private Integer active;
}

