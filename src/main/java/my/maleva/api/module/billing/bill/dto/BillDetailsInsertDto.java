package my.maleva.api.module.billing.bill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One bill line as the screen sends it.
 *
 * <p>Unknown properties are ignored because the grid posts its whole row back,
 * including display-only columns (ProductCode, ProductName, EditMode, SNo)
 * that are not persisted.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillDetailsInsertDto {

    /** Existing detail row id; 0 or null for a new line. */
    private Integer id;

    /** GLAccounts.RowIndex — the expense account the line posts to. */
    private Integer accountMasterRefId;

    private Float mrp;
    private Float purchaseRate;
    private Float itemQty;
    private Float discPer;
    private Float discAmount;
    private Float landingCost;
    private Float taxPercent;
    private Float taxAmount;
    private Float salesRate;
    private Float netSalesRate;
    private Float amount;
    private String remarksD;
    private Float currencyValue;
    private Float actualAmount;
    /** Rate in the supplier's currency, before conversion. */
    private Float actualAmount1;
}
