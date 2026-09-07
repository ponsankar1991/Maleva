package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One document the credit note is knocked off against: either an invoice
 * ({@code saleMasterRefId}) or the customer's opening balance
 * ({@code customeropenRefId}). Exactly one of the two is set.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditKnockOffRequest {

    private Integer saleMasterRefId;

    private Integer customeropenRefId;

    /** What this credit note takes off that document. Zero rows are dropped. */
    private BigDecimal saleCreditAmount;
}
