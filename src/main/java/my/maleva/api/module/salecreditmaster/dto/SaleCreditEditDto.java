package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * A saved credit note loaded back into the entry screen — the port of legacy
 * {@code EditSaleCredit}.
 *
 * <p>{@code knockOffs} is the customer's whole outstanding list with this
 * credit note's amounts merged back in (so the clerk can move the credit
 * between documents), not only the rows that were saved. Legacy did the same
 * but matched invoice rows only, which silently dropped an opening-balance
 * knock-off on every edit; both keys are matched here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditEditDto {

    private Integer id;
    private Integer companyRefId;
    private Integer customerRefId;
    private String customerName;
    private Integer employeeRefId;
    private Integer userRefId;

    private Integer cNumber;
    private String cNumberDisplay;

    /** yyyy-MM-dd for the date input. */
    private String saleDate;
    /** dd/MM/yyyy for display. */
    private String sSaleDate;

    /** The invoice this credit note is against, and its number. */
    private Integer saleMasterRefId;
    private String saleNo;

    private BigDecimal amount;
    private BigDecimal grossAmount;
    private BigDecimal taxAmount;
    private BigDecimal coinage;
    private Double currencyValue;
    private BigDecimal actualAmount;

    private String remarks;
    private Integer cStatus;

    private String qneCode;
    private String qneId;
    private String eInvoiceUid;
    private String eInvoiceStatus;

    private List<SaleCreditEditLineDto> details;

    private List<SaleCreditBillDto> knockOffs;
}
