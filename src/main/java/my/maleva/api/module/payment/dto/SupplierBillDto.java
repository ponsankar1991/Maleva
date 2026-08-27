package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * One outstanding document in the Pay Bills grid — the Java shape of
 * {@code RT_SupplierBills}.
 *
 * <p>{@code balance} is what is still owed once every <em>other</em> payment is
 * counted; the payment being edited is excluded, so re-opening a payment shows
 * the bill as still owing the amount that payment settles.
 *
 * <p>{@code amount} is what this payment puts against the document: 0 on a
 * fresh screen, and the previously saved figure when a payment is reopened.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierBillDto {

    private Integer billMasterRefId;

    private Integer purchaseMasterRefId;

    private Integer supplieropenRefId;

    private String billNo;

    private String invoiceNo;

    /** {@code dd/MM/yyyy}, as the grid renders it. */
    private String sBillDate;

    private BigDecimal billAmount;

    /** Already settled by other payments. */
    private BigDecimal payment;

    private BigDecimal balance;

    private BigDecimal amount;
}
