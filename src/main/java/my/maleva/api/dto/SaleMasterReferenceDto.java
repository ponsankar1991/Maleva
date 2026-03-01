package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SaleMasterReferenceDto - DTO for SaleMasterReference
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleMasterReferenceDto {

    private Integer id;

    @NotNull(message = "Sale Master Reference ID is required")
    private Integer saleMasterRefId;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;
}

