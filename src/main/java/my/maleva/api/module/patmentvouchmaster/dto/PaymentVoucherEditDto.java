package my.maleva.api.module.patmentvouchmaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** One payment voucher, loaded back into the screen. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherEditDto {

    private Integer id;

    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer cNumber;

    private String cNumberDisplay;

    private Integer paymentById;

    private LocalDateTime paymentVoucherDate;

    /** {@code dd/MM/yyyy}, for the date picker. */
    private String sPaymentVoucherDate;

    private String payTo;

    private String payFrom;

    private String description;

    private String refNo;

    private String paymentStatus;

    private BigDecimal amount;

    private BigDecimal bankCharges;

    private Integer billsOrderMasterRefId;

    /** {@code Payment_Receipt_Info.Id} stored on the voucher. */
    private Integer selectedPaytoid;

    private String qneCode;

    private String qneId;

    private List<PaymentVoucherEditLineDto> paymentVoucherDetails;
}
