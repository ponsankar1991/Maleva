package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One row of the payment F5 grid. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMasterViewDto {

    private Integer id;

    /** The running number as an integer. */
    private Integer billNo;

    /** The running number as shown, e.g. {@code PY000000042}. */
    private String billNoDisplay;

    /** Payment date, {@code dd/MM/yyyy}. */
    private String billDate;

    /** When the row was entered, {@code dd/MM/yyyy hh:mm:ss}. */
    private String billTime;

    private String supplierName;

    private String employeeName;

    private String bankName;

    private String description;

    private String payTo;

    /**
     * Exact to the sen. {@code Payment.Amount} is {@code numeric(18,2)} in the
     * database, so it is carried as BigDecimal rather than a float — a company
     * total runs past 10 million, which needs 9 significant digits and a
     * 32-bit float only carries about 7.
     */
    private BigDecimal amount;

    private String refNumber;

    private String remarks;

    private String paymentStatus;

    /** 1 once the payment has been pushed to the payment-voucher queue. */
    private Integer pvStatus;

    private String qneCode;

    private String qneId;
}
