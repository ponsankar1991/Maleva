package my.maleva.api.module.patmentvouchmaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One row of the voucher F5 grid — the columns the legacy view sent. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherMasterViewDto {

    private Integer id;

    private Integer cNumber;

    private String cNumberDisplay;

    /** Voucher date, {@code dd/MM/yyyy}. */
    private String sPaymentVoucherDate;

    private String employeeName;

    /** The paying bank's name ({@code BankMaster.Name}). */
    private String paymentByIdName;

    private String payTo;

    private String description;

    private String refNo;

    private String paymentStatus;

    private BigDecimal amount;

    private String qneCode;

    private String qneId;

    private String eInvoiceUid;
}
