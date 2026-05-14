package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Response DTO for CheckEditAmount operation
 * Contains the calculated total payment amount for a purchase order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckEditAmountResponseDto {

    private BigDecimal totalPaymentAmount;
    private String message;
}
