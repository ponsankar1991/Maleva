package my.maleva.api.module.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * What {@code SP_SaleMaster} hands back after a successful save.
 *
 * <p>The procedure only sets its {@code @SaleNoDisplay} variable on the insert
 * branch, so an edit returns a null BillNo; the service reads the stored
 * CNumberDisplay in that case rather than passing the null through.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleInvoiceSaveResult {

    /** SaleMaster.Id - newly allocated on insert, echoed back on edit. */
    private Integer id;

    /** SaleMaster.CNumberDisplay, e.g. {@code INV000000123}. */
    private String billNo;

    /** SaleMaster.CNumber, the numeric form behind billNo. */
    private Integer billNumber;

    /** The procedure's GETDATE() stamp for the save. */
    private LocalDateTime saleTime;

    /** True when this save created the invoice rather than re-writing one. */
    private boolean created;
}
