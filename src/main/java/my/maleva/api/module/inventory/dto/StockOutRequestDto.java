package my.maleva.api.module.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockOutRequestDto {

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotNull(message = "Product Reference ID is required")
    private Integer productRefId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @Size(max = 50, message = "Reference Type cannot exceed 50 characters")
    private String referenceType;

    private Integer referenceId;

    private Integer truckRefId;

    @Size(max = 100, message = "Asset Serial No cannot exceed 100 characters")
    private String assetSerialNo;

    @Size(max = 200, message = "Remarks cannot exceed 200 characters")
    private String remarks;

    @NotBlank(message = "Created By is required")
    @Size(max = 50, message = "Created By cannot exceed 50 characters")
    private String createdBy;
}
