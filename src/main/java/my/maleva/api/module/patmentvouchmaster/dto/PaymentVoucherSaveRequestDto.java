package my.maleva.api.module.patmentvouchmaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the Payment Voucher screen posts on Save — the Java shape of the JSON
 * blob legacy handed to {@code SP_PaymentVoucherMaster}.
 *
 * <p>{@code id} 0 or absent inserts; anything else updates that voucher.
 * {@code cNumber}/{@code cNumberDisplay} are deliberately absent: the running
 * number is assigned by the server on insert and never re-pointed on edit —
 * the SP's UPDATE branch has both columns commented out.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherSaveRequestDto {

    private Integer id;

    private Integer userRefId;

    private Integer employeeRefId;

    /** The paying bank ({@code BankMaster.Id}); the screen's "Payment By". */
    private Integer paymentById;

    /** ISO {@code yyyy-MM-dd}; {@code dd/MM/yyyy} also accepted. */
    private String paymentVoucherDate;

    private String payTo;

    private String payFrom;

    private String description;

    /** Cheque / reference number. */
    private String refNo;

    private String paymentStatus;

    private BigDecimal amount;

    private BigDecimal bankCharges;

    private BigDecimal currencyValue;

    private BigDecimal actualAmount;

    /** The purchase order this voucher settles, when pushed across from one. */
    private Integer billsOrderMasterRefId;

    /**
     * {@code Payment_Receipt_Info.Id} matched from the PayTo name — the SP
     * stores it as {@code paymentReceiptid}, and the e-Invoice flow reads it.
     */
    private Integer selectedPaytoid;

    private List<PaymentVoucherSaveLineDto> paymentVoucherDetails;
}
