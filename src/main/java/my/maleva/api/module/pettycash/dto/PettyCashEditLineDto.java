package my.maleva.api.module.pettycash.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One petty cash line as the edit screen renders it. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashEditLineDto {

    private Integer id;

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
