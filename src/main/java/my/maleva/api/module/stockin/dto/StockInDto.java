package my.maleva.api.module.stockin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * StockInDto - DTO for StockIn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockInDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer saleOrderMasterRefId;

    @NotNull(message = "Stock Date is required")
    private LocalDateTime stockDate;

    @NotBlank(message = "C Number Display is required")
    @Size(max = 300, message = "C Number Display must not exceed 300 characters")
    private String cNumberDisplay;

    @NotNull(message = "C Number is required")
    private Integer cNumber;

    @NotNull(message = "Number of Packages is required")
    private Integer numberOfPackages;

    @NotNull(message = "Port Master Reference ID is required")
    private Integer portMasterRefId;

    @Size(max = 200, message = "Barcode must not exceed 200 characters")
    private String barcode;

    @Size(max = 200, message = "Barcode Label Display must not exceed 200 characters")
    private String barcodeLabelDisplay;

    private LocalDateTime createdDate;

    private String createdBy;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    @NotNull(message = "Status is required")
    private Integer status;

    private LocalDateTime warehouseDate;
}

