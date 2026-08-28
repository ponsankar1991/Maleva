package my.maleva.api.module.patmentvouchmaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for the voucher F5 grid.
 *
 * <p>A non-blank {@code search} matches the voucher number and, as in legacy,
 * overrides every other filter (its WHERE-building reset the clause to empty
 * first) — so a clerk can pull up one voucher without knowing when it was made
 * or who entered it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectPaymentVoucherRequestDto {

    private Integer comid;

    /** ISO {@code yyyy-MM-dd}; {@code dd/MM/yyyy} also accepted. */
    private String fromDate;

    private String toDate;

    private Integer employeeId;

    private String paymentStatus;

    private String payTo;

    private String description;

    /** Voucher number — overrides the date range when set. */
    private String search;
}
