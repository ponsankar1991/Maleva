package my.maleva.api.module.transactionreport.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One settled payment on the Payment Completed grid.
 *
 * <p>The union has two arms and {@link #detailedId} is what tells them apart:
 * {@code 0} is a row from {@code PaymentVoucherMaster}, {@code 1} a row from
 * {@code Payment}. It decides which screen the row opens and which upload
 * folder its documents live in, so it is not cosmetic.
 *
 * <p>Field names keep the legacy column aliases ({@code sSaleDate},
 * {@code filePath}) even where they are misnomers — {@code filePath} is the
 * expense Description and has never held a path — because the two arms are
 * aliased into one shape and renaming one half would break the union's
 * symmetry with {@code dashboard}'s already-migrated {@code CompletedPaymentDto}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDoneRowDto {

    /** Row id within its own register — unique per {@link #detailedId}, not globally. */
    private Integer id;

    /** Payment date, already formatted {@code dd/MM/yyyy} by the query. */
    private String sSaleDate;

    /** Voucher / payment number as displayed. The grid's default sort. */
    private String cNumberDisplay;

    /** PayTo on a voucher, SupplierName on a supplier payment. */
    private String expenseName;

    private String refNumber;

    /** {@code numeric} at rest on both arms — BigDecimal end to end, never Float. */
    private BigDecimal amount;

    /** QNE code. Aliased {@code Remarks} by the legacy query; the grid labels it "QNE Code". */
    private String remarks;

    /** 0 = payment voucher, 1 = supplier payment. */
    private Integer detailedId;

    /** Expense category ({@code Description}). Legacy aliased it {@code FilePath}. */
    private String filePath;

    /**
     * Sum of every voucher sharing this row's Description, company-wide and
     * unbounded by date. Null on the supplier arm — legacy selected a literal
     * NULL there rather than computing the equivalent.
     */
    private BigDecimal totalAmount;
}
