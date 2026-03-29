package my.maleva.api.module.paymentrecept.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * ReceiptDetailsDto
 * Data Transfer Object for ReceiptDetails API layer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDetailsDto {

    private Integer id;

    private Integer companyRefId;

    @NotNull(message = "Receipt Reference ID is required")
    private Integer receiptRefId;

    private Integer saleMasterRefId;

    private Integer customerOpenRefId;

    @NotNull(message = "Receipt Amount is required")
    @DecimalMin(value = "0.0", message = "Receipt Amount must be 0 or greater")
    private BigDecimal receiptAmount;

    private LocalDateTime createdDate;

    @Min(value = 0, message = "Currency Value must be 0 or greater")
    private Double currencyValue;

    @Min(value = 0, message = "Actual Amount must be 0 or greater")
    private Double actualAmount;
}

