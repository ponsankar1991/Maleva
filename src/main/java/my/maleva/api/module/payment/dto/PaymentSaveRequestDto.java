package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the Pay Bills screen posts when the clerk clicks Save — the Java shape
 * of the JSON blob legacy handed to {@code SP_Payment}.
 *
 * <p>{@code id} 0 or absent inserts; anything else updates that payment.
 * {@code cNumber}/{@code cNumberDisplay} are deliberately absent: the running
 * number is assigned by the server on insert and never re-pointed on edit,
 * exactly as SP_Payment did (its UPDATE branch has those two columns commented
 * out).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSaveRequestDto {

    private Integer id;

    private Integer supplierRefId;

    private Integer bankRefId;

    private Integer employeeRefId;

    private Integer userRefId;

    /**
     * ISO {@code yyyy-MM-dd} (or a full ISO timestamp). {@code dd/MM/yyyy} is
     * accepted because that is what the screen displays; {@code MM/dd/yyyy} is
     * rejected rather than guessed at.
     */
    private String paymentDate;

    private String refNumber;

    private String payTo;

    private String description;

    private String remarks;

    private String paymentStatus;

    /** 1 sends the payment on to the payment-voucher approval queue. */
    private Integer pvStatus;

    private BigDecimal amount;

    private Float bankCharges;

    private Float currencyValue;

    private Float actualAmount;

    private String tinNo;

    private String sstNo;

    private String msicCode;

    private String serviceTaxType;

    private String bankName;

    private String accountNo;

    /** The bills this payment settles. Rows with a zero amount are ignored. */
    private List<PaymentSaveDetailDto> paymentDetails;
}
