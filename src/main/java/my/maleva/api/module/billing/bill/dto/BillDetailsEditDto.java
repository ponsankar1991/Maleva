package my.maleva.api.module.billing.bill.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One bill line as the edit screen reloads it: the stored values plus the
 * account code and name the grid displays, resolved from GLAccounts.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDetailsEditDto {

    private Integer id;
    private Integer billMasterRefId;
    private Integer accountMasterRefId;
    /** GLAccounts.GLAccountCode — the grid's "Account Code" column. */
    private String productCode;
    /** GLAccounts.Description — the grid's "Account Name" column. */
    private String productName;
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
    private Float actualAmount1;
}
