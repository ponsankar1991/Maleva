package my.maleva.api.module.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

/**
 * InventoryFilterDto - DTO for inventory filtering and reporting
 * Equivalent to .NET InventoryModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryFilterDto {

    private String fromDate;

    private String toDate;

    @NotNull(message = "Company ID is required")
    private Integer comid;

    private Integer status;

    private Integer customerId;

    private Integer portType;
}

