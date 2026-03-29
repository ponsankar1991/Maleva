package my.maleva.api.module.saleorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SportSaleOrderDto - DTO for SportSaleOrder
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportSaleOrderDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer customerRefId;

    @NotNull(message = "Job Master Reference ID is required")
    private Integer jobMasterRefId;

    private Integer employeeRefId;

    @Size(max = 100, message = "AWB No must not exceed 100 characters")
    private String awbNo;

    @Size(max = 100, message = "BL Copy must not exceed 100 characters")
    private String blCopy;

    @Size(max = 100, message = "Quantity must not exceed 100 characters")
    private String quantity;

    @Size(max = 100, message = "Total Weight must not exceed 100 characters")
    private String totalWeight;

    private Integer jStatus;

    @Size(max = 500, message = "DO Description must not exceed 500 characters")
    private String doDescription;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private Integer saleOrderMasterRefId;

    @Size(max = 100, message = "Vehicle Name must not exceed 100 characters")
    private String vehicleName;

    private Integer active;

    @Size(max = 500, message = "Port must not exceed 500 characters")
    private String port;

    @Size(max = 300, message = "Document Path must not exceed 300 characters")
    private String documentPath;
}

