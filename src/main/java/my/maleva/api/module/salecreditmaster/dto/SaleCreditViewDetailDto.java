package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One product line under an expanded view row.
 *
 * <p>{@code taxAmount} is the LINE's tax. The legacy query selected
 * {@code A.TaxAmount as TaxAmt} where {@code A} is the master, so every line
 * of a note showed the note's whole tax.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditViewDetailDto {

    private Integer detailId;
    /** SaleCreditMaster.Id — the parent row. */
    private Integer saleRefId;
    private String productCode;
    private String productName;
    private BigDecimal itemQty;
    private BigDecimal saleRate;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal amount;
}
