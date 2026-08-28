package my.maleva.api.module.pettycash.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for the petty cash F5 grid.
 *
 * <p>A non-blank {@code search} matches only {@code CNumberDisplay} and
 * overrides every other filter, exactly like {@code SelectPaymentVoucherRequestDto}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectPettyCashRequestDto {

    private Integer comid;

    /** ISO {@code yyyy-MM-dd}; {@code dd/MM/yyyy} also accepted. */
    private String fromDate;

    private String toDate;

    private Integer employeeId;

    private String paymentStatus;

    /** CNumberDisplay — overrides every other filter when set. */
    private String search;
}
