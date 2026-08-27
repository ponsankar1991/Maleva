package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for the payment F5 grid.
 *
 * <p>A non-blank {@code search} matches the payment number and, as in legacy,
 * overrides every other filter — so a clerk can pull up one payment without
 * knowing when it was made or who entered it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectPaymentRequestDto {

    private Integer comid;

    /** ISO {@code yyyy-MM-dd}; {@code dd/MM/yyyy} also accepted. */
    private String fromDate;

    private String toDate;

    /** Supplier filter. */
    private Integer supplierId;

    private Integer employeeId;

    private String paymentStatus;

    private String description;

    /** Payment number — overrides the other filters when set. */
    private String search;
}
