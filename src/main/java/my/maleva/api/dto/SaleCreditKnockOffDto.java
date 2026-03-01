package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SaleCreditKnockOffDto
 * Data Transfer Object for SaleCreditKnockOff API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleCreditKnockOffDto {

    private Integer id;

    private Integer companyRefId;

    @NotNull(message = "Sale Credit Master Reference ID is required")
    private Integer saleCreditMasterRefId;

    private Integer saleMasterRefId;

    private Integer customerOpenRefId;

    @NotNull(message = "Sale Credit Amount is required")
    @Min(value = 0, message = "Sale Credit Amount must be greater than or equal to 0")
    private BigDecimal saleCreditAmount;

    private LocalDateTime createdDate;

    private Double currencyValue;

    private Double actualAmount;
}

