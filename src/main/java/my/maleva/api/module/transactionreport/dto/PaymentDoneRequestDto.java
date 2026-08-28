package my.maleva.api.module.transactionreport.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Filters for the Payment Completed grid.
 *
 * <p>The Java port of the legacy {@code SubExpenseWindowModel} as
 * {@code TransactionReport/SelectPaymentDone} actually used it. Legacy sent
 * fifteen separate integer fields ({@code SupplierId1} … {@code SupplierId15}),
 * each carrying its own ordinal when its checkbox was ticked, and the service
 * turned that back into a list of {@code Description} strings. The ordinals
 * never meant anything on their own, so this contract takes the list directly.
 *
 * <p>The legacy numbering also carried a bug worth naming: the Wages checkbox
 * set {@code SupplierId15 = 14} while the service tested {@code == 15}, so
 * ticking Wages silently filtered nothing. Passing names removes the class of
 * mistake rather than reproducing it.
 *
 * <p>{@code supplierId} and {@code payTo} are mutually exclusive by design, not
 * by accident — see {@code TransactionReportRepository#findCompletedPayments}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDoneRequestDto {

    /** Company scope. Also accepted as a {@code companyId} request param or {@code Comid} header. */
    private Integer comid;

    /** ISO {@code yyyy-MM-dd}; {@code dd/MM/yyyy} is also accepted. */
    private String fromDate;

    /** ISO {@code yyyy-MM-dd}; {@code dd/MM/yyyy} is also accepted. */
    private String toDate;

    /**
     * Supplier filter. Non-zero narrows to supplier payments only — the voucher
     * arm of the union is suppressed, because a voucher has no supplier.
     */
    private Integer supplierId;

    /**
     * PayTo filter. Non-blank narrows to payment vouchers only, for the mirror
     * reason. Ignored when {@code supplierId} is set, matching legacy.
     */
    private String payTo;

    /**
     * Expense categories to keep, matched against {@code Description} on both
     * arms. Empty or null means every category. The values are the exact
     * strings stored on the rows: HIRE PURCHASE, VENDOR, UTILITY, TENANCY,
     * MAINTENANCE, SALARY, OTHER EXPENSES, DIRECTOR EXPENSES, BACKLOG, FUEL,
     * TOLL, CLAIM, KASTAM DUTY, Wages.
     */
    private List<String> descriptions;
}
