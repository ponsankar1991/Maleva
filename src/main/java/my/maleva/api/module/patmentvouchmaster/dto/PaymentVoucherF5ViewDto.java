package my.maleva.api.module.patmentvouchmaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * The voucher F5 grid: matching vouchers plus every line beneath them, which
 * the grid nests by {@code paymentVoucherMasterRefId}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherF5ViewDto {

    private List<PaymentVoucherMasterViewDto> paymentVoucherMaster;

    private List<PaymentVoucherDetailViewDto> paymentVoucherDetails;

    /**
     * What the listed vouchers come to, summed by the database. The Amount
     * column is {@code real} at rest, so this is exact to what is stored —
     * still better than re-adding floats in the browser.
     */
    private BigDecimal totalAmount;

    private Integer count;
}
