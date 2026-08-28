package my.maleva.api.module.patmentvouchmaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One expense line of a payment voucher. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherSaveLineDto {

    /** The GL account, as {@code GLAccounts.RowIndex}. */
    private Integer accountGroupRefId;

    private String description;

    private BigDecimal amount;

    private BigDecimal currencyValue;

    private BigDecimal actualAmount;

    /** LHDN e-Invoice classification id, when set on the line. */
    private Integer classification;

    /**
     * Links the line to a recurring sub-expense; the save then claims the
     * earliest unclaimed PendingPayment slot due this month for it.
     */
    private Integer subExpenseRefid;
}
