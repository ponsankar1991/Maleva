package my.maleva.api.module.billing.bill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters for the bill F5 search.
 *
 * <p>A non-empty {@code search} overrides everything else — legacy matched it
 * against the bill number or the supplier invoice number and dropped the date
 * and supplier filters entirely, so a clerk can find one bill without knowing
 * when it was raised.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectBillMasterRequestDto {

    private Integer comid;
    /** dd/MM/yyyy or yyyy-MM-dd. */
    private String fromdate;
    private String todate;
    /** Supplier id; 0 or null means every supplier. */
    private Integer id;
    /** Employee id; 0 or null means every employee. */
    private Integer employeeid;
    private String search;
    /** 1 filters on the supplier invoice date instead of the bill date. */
    private Integer invoicecheck;
}
