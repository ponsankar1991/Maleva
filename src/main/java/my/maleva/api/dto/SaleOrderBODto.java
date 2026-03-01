package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleOrderBODto - DTO for SaleOrderBO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderBODto {

    private Integer id;

    @NotNull(message = "Sale Order Master Reference ID is required")
    private Integer saleOrderMasterRefId;

    @NotNull(message = "BO Type ID is required")
    private Integer boTypeId;

    @NotNull(message = "Status is required")
    private Integer status;

    private LocalDateTime createdDate;
}

