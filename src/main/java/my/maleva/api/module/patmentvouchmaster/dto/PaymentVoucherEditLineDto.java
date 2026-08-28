package my.maleva.api.module.patmentvouchmaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One voucher line as the edit screen renders it, account labels included. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherEditLineDto {

    private Integer id;

    private Integer accountGroupRefId;

    /** {@code GLAccounts.GLAccountCode} for the line's account. */
    private String accountCode;

    /** {@code GLAccounts.Description}. */
    private String accountName;

    private String description;

    private BigDecimal amount;

    private Integer classification;

    private String classificationName;

    private Integer subExpenseRefid;

    private Integer pendingPaymentRefId;
}
