package my.maleva.api.module.pettycash.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One expense line beneath a petty cash record in the F5 grid's expandable row. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashDetailViewDto {

    private Integer id;

    /** The petty cash record this line belongs to — the grid nests its child rows on this. */
    private Integer pettyCashMasterRefId;

    private String items;

    private BigDecimal amount;

    private String notes;

    /** Optional chart-of-accounts account — a {@code GLAccounts.RowIndex}. */
    private Integer accountGroupRefId;

    /** {@code GLAccounts.GLAccountCode} for {@link #accountGroupRefId}, resolved by join. */
    private String accountCode;

    /** {@code GLAccounts.Description} for {@link #accountGroupRefId}, resolved by join. */
    private String accountName;

    /** Optional e-Invoice classification — a {@code Classification.Id}. */
    private Integer classification;

    /** {@code Classification.Description} for {@link #classification}, resolved by join. */
    private String classificationName;
}
