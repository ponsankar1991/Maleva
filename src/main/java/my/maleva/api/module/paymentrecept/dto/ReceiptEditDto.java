package my.maleva.api.module.paymentrecept.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * A saved receipt loaded back into the entry screen — the port of legacy
 * {@code EditReceipt}.
 *
 * <p>{@code receiptDetails} is the customer's whole outstanding list with this
 * receipt's amounts merged onto the documents it settles, not just the saved
 * lines, which is what lets the clerk move money between documents and
 * re-save.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptEditDto {
    private Integer id;
    private Integer companyRefId;
    private Integer customerRefId;
    private Integer bankRefId;
    private Integer employeeRefId;
    private Integer lastEmployeeRefId;
    private Integer cNumber;
    private String cNumberDisplay;
    /** ISO yyyy-MM-dd */
    private String receiptDate;
    /** dd/MM/yyyy */
    private String sReceiptDate;
    private BigDecimal amount;
    private Double currencyValue;
    private Double actualNetAmount;
    private Double bankCharges;
    private String remarks;
    private String refNumber;
    private Integer pvStatus;
    private Integer fileUpload;
    private String qneCode;
    private String qneId;
    private List<ReceiptBillDto> receiptDetails;
}
