package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One settled line of a payment: which document is being paid, and how much of
 * it. Exactly one of the three reference ids is set — a bill, a purchase order,
 * or the supplier's opening balance.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSaveDetailDto {

    private Integer billMasterRefId;

    private Integer purchaseMasterRefId;

    private Integer supplieropenRefId;

    /** How much of this document the payment settles. */
    private BigDecimal amount;

    private Float currencyValue;

    private Float actualAmount;
}
