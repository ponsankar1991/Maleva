package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One outstanding document of the customer, for the credit note's knock-off
 * grid — the port of the legacy {@code RT_CustomerBills} rows the screen
 * loaded through {@code /Receipt/SelectCustomerBills}.
 *
 * <p>{@code saleCreditAmount} is what this credit note puts against the
 * document: 0 on a fresh load, and the saved amount when a credit note is
 * opened for edit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditBillDto {

    /** Set for an invoice row; null for the opening-balance row. */
    private Integer saleMasterRefId;

    /** Set for the opening-balance row; null for an invoice row. */
    private Integer customeropenRefId;

    private String billNo;

    /** dd/MM/yyyy, blank for the opening balance. */
    private String sBillDate;

    private BigDecimal billAmount;

    /** Already settled by receipts and other credit notes. */
    private BigDecimal settled;

    private BigDecimal balance;

    private BigDecimal saleCreditAmount;

    private BigDecimal currencyValue;

    private BigDecimal actualAmount;

    /** SaleCreditKnockOff.Id when this row came from the credit note being edited. */
    private Integer sdId;
}
