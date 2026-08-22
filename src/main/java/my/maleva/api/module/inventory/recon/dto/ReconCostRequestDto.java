package my.maleva.api.module.inventory.recon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One line of repair spend.
 *
 * Amount is not accepted from the caller - the service computes quantity times
 * rate, so a line total can never disagree with its own figures.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconCostRequestDto {

    @NotBlank(message = "Cost type is required")
    private String costType;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    /**
     * Set when the line is a part taken from the workshop store. The service
     * then issues that quantity out of stock against this job.
     */
    private Integer productRefId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", message = "Rate cannot be negative")
    private BigDecimal rate;

    private Integer supplierRefId;

    @Size(max = 50, message = "Doc No cannot exceed 50 characters")
    private String docNo;

    private LocalDateTime docDate;

    @Size(max = 200, message = "Remarks cannot exceed 200 characters")
    private String remarks;

    @NotBlank(message = "Created By is required")
    @Size(max = 50, message = "Created By cannot exceed 50 characters")
    private String createdBy;
}
