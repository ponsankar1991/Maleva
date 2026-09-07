package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One row of the view grid. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditViewRowDto {

    private Integer id;
    private Integer billNo;
    private String billNoDisplay;
    /** dd/MM/yyyy. */
    private String billDate;
    /** dd/MM/yyyy HH:mm:ss — when the note was created. */
    private String billTime;
    private String employeeName;
    private Integer customerRefId;
    private String customerName;
    /** The invoice the note was raised against. */
    private String invoiceNo;
    private BigDecimal amount;
    private String remarks;
    private String qneCode;
    private String qneId;
    private String eInvoiceUid;
    private String eInvoiceStatus;

    /** The same total on every row (SUM OVER), so the grid needs one round trip. */
    private BigDecimal totalAmount;
}
