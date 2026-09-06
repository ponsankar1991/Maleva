package my.maleva.api.module.paymentrecept.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One receipt in the RECEIPT ENTRY VIEW grid (legacy {@code ReceiptMasterViewModel}). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptViewRowDto {
    private Integer id;
    private Integer billNo;
    private String billNoDisplay;
    /** dd/MM/yyyy */
    private String billDate;
    /** dd/MM/yyyy HH:mm:ss of Created_Date */
    private String billTime;
    private String employeeName;
    private Integer customerRefId;
    private String customerName;
    /** BankMaster.AccountName — the "Received By" column. */
    private String bankName;
    private String refNumber;
    private BigDecimal amount;
    private String remarks;
    private String qneCode;
    private String qneId;
    private Integer pvStatus;
    private Integer fileUpload;
    /** True when the receipt's attachment folder holds a file (legacy red row). */
    private boolean hasAttachments;
}
