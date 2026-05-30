package my.maleva.api.module.patmentvouchmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PaymentVoucherComboDto - DTO for distinct PayTo values
 * Used in SelectPaymentTo endpoint response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherComboDto {
    private String accountName;
}

