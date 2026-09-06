package my.maleva.api.module.invoice.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * What the Sale Invoice view screen asks for — the port of the legacy
 * {@code SaleInvoiceF5ViewModel}, with the field names spelled out.
 *
 * <p>Legacy name → here: {@code Comid} companyId, {@code Id} customerId,
 * {@code JId} jobTypeId, {@code Employeeid} employeeId, {@code Statusid}
 * statusId, {@code completestatusnotshow} hideCompleted, {@code Remarks}
 * remarksFilter, {@code Search} + {@code Invoice} search / searchByJobNo,
 * {@code checkqnepush} unpushedOnly, {@code ETA}/{@code ETAType} eta / etaType,
 * {@code Pickup} pickup. {@code SoId}, {@code DId}, {@code TId} and {@code Id1}
 * were never read by the query and are gone.
 *
 * <p>Every flag and count is a wrapper type so a screen that sends
 * {@code null} (or omits the key) gets the legacy default — false or 0 —
 * instead of a 400 from Jackson. Read the flags through the {@code isX()}
 * accessors, which apply that default.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleInvoiceViewFilter {

    private Integer companyId;
    private LocalDate fromDate;
    private LocalDate toDate;

    private Integer customerId;
    private Integer jobTypeId;
    private Integer employeeId;
    private Integer statusId;

    /** Hide invoices whose job status is 8 (completed). */
    private Boolean hideCompleted;

    /** 0 = all, 1 = only invoices with remarks, 2 = only invoices without. */
    private Integer remarksFilter;

    private String offVesselName;
    private String loadingVesselName;

    /** Exact invoice number (or job number when {@link #isSearchByJobNo()}); overrides every other filter, as legacy did. */
    private String search;
    private Boolean searchByJobNo;

    /** Only invoices not yet pushed to QNE; overrides every other filter, as legacy did. */
    private Boolean unpushedOnly;

    /** Date range applies to the vessel ETA instead of the invoice date. */
    private Boolean eta;
    /** 1 = off-vessel ETA, 2 = loading-vessel ETA, anything else = either. */
    private Integer etaType;
    /** Date range applies to the pickup date instead of the invoice date. */
    private Boolean pickup;

    public boolean isHideCompleted() {
        return Boolean.TRUE.equals(hideCompleted);
    }

    public boolean isSearchByJobNo() {
        return Boolean.TRUE.equals(searchByJobNo);
    }

    public boolean isUnpushedOnly() {
        return Boolean.TRUE.equals(unpushedOnly);
    }

    public boolean isEta() {
        return Boolean.TRUE.equals(eta);
    }

    public boolean isPickup() {
        return Boolean.TRUE.equals(pickup);
    }

    public int remarksFilterOrDefault() {
        return remarksFilter == null ? 0 : remarksFilter;
    }

    public int etaTypeOrDefault() {
        return etaType == null ? 0 : etaType;
    }
}
