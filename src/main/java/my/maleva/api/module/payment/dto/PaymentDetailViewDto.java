package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One settled document beneath a payment in the F5 grid's expandable row.
 *
 * <p>{@code purchaseNo}/{@code sPurchaseDate} carry whichever document the line
 * actually settles: legacy filled them from the purchase order when there was
 * one and fell back to the bill otherwise, and the grid reads only those two
 * columns.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetailViewDto {

    /** The payment this line belongs to — the grid joins its child rows on this. */
    private Integer saleRefId;

    private Integer pdId;

    private String purchaseNo;

    private String sPurchaseDate;

    private String billNo;

    private String sBillDate;

    private String dSupplierName;

    /** Exact to the sen — {@code PaymentDetails.PaymentAmount} is {@code numeric(18,2)}. */
    private BigDecimal paymentAmount;
}
