package my.maleva.api.module.paymentrecept.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the SEND RECEIPT window shows before the operator presses SEND — the
 * port of legacy {@code ReceiptMailInfo}: the receipt header, the customer's
 * mail ids, the default CC list, the subject, and the files already attached
 * to the receipt that can be sent along with the voucher PDF.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptMailInfoDto {
    private Integer receiptId;
    private String receiptNo;
    /** dd/MM/yyyy */
    private String receiptDate;
    private String customerName;
    private String refNumber;
    private BigDecimal amount;
    private String currencySymbol;
    /** Every non-empty customer mail id (AEmail, AEmail1, OEmail, OEmail1), no duplicates. */
    private List<String> customerEmails;
    private List<String> defaultCc;
    private String subject;
    /** Names of the files in the receipt's attachment folder. */
    private List<String> attachmentFiles;
    private boolean mailConfigured;
}
