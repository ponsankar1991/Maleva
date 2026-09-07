package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One knocked-off document under an expanded view row. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditViewKnockOffDto {

    private Integer knockOffId;
    /** SaleCreditMaster.Id — the parent row. */
    private Integer saleRefId;
    /** The invoice number, or blank for an opening-balance knock-off. */
    private String saleNo;
    /** dd/MM/yyyy, blank for an opening balance. */
    private String sSaleDate;
    /** The customer whose opening balance was knocked off, when that is the row. */
    private String openingBalanceCustomer;
    private BigDecimal saleCreditAmount;
}
