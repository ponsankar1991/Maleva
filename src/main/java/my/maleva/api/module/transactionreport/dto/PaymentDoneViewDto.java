package my.maleva.api.module.transactionreport.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * The Payment Completed grid: matching rows, their total, and the row count.
 *
 * <p>{@link #totalAmount} is a SQL {@code SUM} over the same filtered union,
 * not a re-add of {@link #rows} in the browser. Both {@code Amount} columns are
 * exact {@code numeric} at rest, and summing them client-side in float is what
 * put an 11.52 RM drift on the payments view — see {@code PaymentF5ViewDto}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDoneViewDto {

    private List<PaymentDoneRowDto> rows;

    private BigDecimal totalAmount;

    private Integer count;
}
