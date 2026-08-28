package my.maleva.api.module.pettycash.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One petty cash expense line as posted on save. No {@code id} — lines are
 * always deleted and reinserted, never updated in place.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashSaveLineDto {

    private String items;

    private BigDecimal amount;

    private String notes;

    /** Optional chart-of-accounts account — a {@code GLAccounts.RowIndex}; 0 means unset. */
    private Integer accountGroupRefId;

    /** Optional e-Invoice classification — a {@code Classification.Id}; 0 means unset. */
    private Integer classification;
}
