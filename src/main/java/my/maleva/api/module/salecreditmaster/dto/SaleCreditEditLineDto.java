package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One saved product line, with the item text the grid shows. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditEditLineDto {

    /** SaleCreditDetails.Id — the grid's row order. */
    private Integer sdId;

    private Integer itemMasterRefId;
    private String productCode;
    private String productName;
    private String uom;

    private BigDecimal itemQty;
    private BigDecimal salesRate;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal amount;
    private BigDecimal currencyValue;
    private BigDecimal actualAmount;
}
