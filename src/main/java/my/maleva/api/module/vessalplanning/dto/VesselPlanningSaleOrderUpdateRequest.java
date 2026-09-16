package my.maleva.api.module.vessalplanning.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body of POST /api/vessel-plannings/sale-order-update - the Vessel Planning screen's Update window.
 *
 * Carries only what that window edits. Totals, line items, customer, the general boarding
 * officers and every other sale order field are absent on purpose, so this save cannot change
 * them.
 */
@Data
@NoArgsConstructor
public class VesselPlanningSaleOrderUpdateRequest {

    @NotNull
    @Positive
    private Integer saleOrderId;

    @NotNull
    @Positive
    private Integer companyId;

    /** JobStatusMaster id. Null or 0 keeps the job's status - the window's status box starts empty. */
    private Integer jobStatusId;

    /** Null or blank keeps the job's cargo - the window's cargo box starts empty. */
    @Size(max = 200)
    private String cargo;

    /** Written as sent; blank clears it. Null keeps it. */
    @Size(max = 100)
    private String ptw;

    /*
     * Loading (ETA/ETB/ETD) and off vessel (OETA/OETB/OETD) dates. Blank means the date's box
     * was unticked and clears the column; null keeps it.
     */
    private String eta;
    private String etb;
    private String etd;
    private String oeta;
    private String oetb;
    private String oetd;

    /*
     * The three loading and three off vessel boarding officers as the window shows them.
     * Null or 0 means no officer in that slot.
     */
    private Integer loadingOfficer1;
    private Integer loadingOfficer2;
    private Integer loadingOfficer3;
    private Integer offOfficer1;
    private Integer offOfficer2;
    private Integer offOfficer3;
}
