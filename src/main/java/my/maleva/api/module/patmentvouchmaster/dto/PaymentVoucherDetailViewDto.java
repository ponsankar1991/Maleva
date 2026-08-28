package my.maleva.api.module.patmentvouchmaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One expense line beneath a voucher in the F5 grid's expandable row. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherDetailViewDto {

    private Integer id;

    /** The voucher this line belongs to — the grid nests its child rows on this. */
    private Integer paymentVoucherMasterRefId;

    private Integer accountGroupRefId;

    private String accountCode;

    private String accountName;

    private String description;

    private BigDecimal amount;
}
