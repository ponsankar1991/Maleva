package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One product line of a credit note as entered.
 *
 * <p>The screen's grid also carried MRP, PurchaseRate, LandingCost, DiscPer,
 * DiscAmount and NetSalesRate. {@code Calculation()} overwrote every one of
 * them with 0 on each keystroke, so they were only ever stored as zero; they
 * are not part of this contract and the service writes 0 for them.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditDetailRequest {

    private Integer itemMasterRefId;

    private Double itemQty;

    private Double salesRate;

    /** GST percentage; the line tax and amount are derived from it. */
    private Double taxPercent;
}
