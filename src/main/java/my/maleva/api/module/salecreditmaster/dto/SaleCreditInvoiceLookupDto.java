package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * The invoice behind the screen's "Invoice No" box.
 *
 * <p>Legacy resolved it by calling {@code /SaleOrder/SelectSaleInvoice} with
 * the number as a free-text search, which loaded the whole invoice with all
 * its lines to read three fields off it. This returns those three fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditInvoiceLookupDto {

    private Integer id;
    private String invoiceNo;
    /** dd/MM/yyyy. */
    private String invoiceDate;
    private Integer customerRefId;
    private String customerName;
    private BigDecimal amount;
}
