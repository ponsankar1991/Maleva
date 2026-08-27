package my.maleva.api.module.billing.bill.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One expanded line under a bill row in the F5 grid. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDetailsViewDto {

    private Float discountAmt;
    private Float discountPercent;
    private Float itemQty;
    private Float mrp;
    private String productName;
    private Float saleRate;
    /** Parent BillMaster id — the grid groups detail rows by this. */
    private Integer saleRefId;
    private Float taxAmt;
    private Float taxPercent;
    private String productCode;
    private Float sAmount;
    private String remarksD;
}
