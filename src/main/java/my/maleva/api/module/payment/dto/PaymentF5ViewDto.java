package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * The payment F5 grid: matching payments plus every line beneath them, which
 * the grid nests by {@code saleRefId}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentF5ViewDto {

    private List<PaymentMasterViewDto> paymentMaster;

    private List<PaymentDetailViewDto> paymentDetails;

    /**
     * What the listed payments come to, summed by the database over
     * {@code numeric(18,2)} and therefore exact to the sen.
     *
     * <p>Deliberately not left to the caller: adding the rows up in the browser
     * means float arithmetic over a figure that runs past 10 million, which is
     * where a total drifts a ringgit from the ledger it is supposed to match.
     */
    private BigDecimal totalAmount;

    /** How many payments matched, so the screen can say so without counting. */
    private Integer count;
}
