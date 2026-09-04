package my.maleva.api.module.saleorderforwardingreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of the forwarding report grid.
 *
 * <p>A sale order carries three forwarding legs side by side in one
 * `SaleOrderMaster` row (`Forwarding`, `Forwarding2`, `Forwarding3` and their
 * companion columns). The query unpivots them into three rows per order, so a
 * row here is <em>one leg of one order</em>, and {@link #fwNo} says which leg it
 * came from — 1, 2 or 3. That number is what the date update needs in order to
 * write back to the right column, so it is carried even though the grid shows
 * it only as a narrow "FW" column.
 *
 * <p>Field names are camelCase; the legacy screen's PascalCase names are not
 * reproduced because nothing outside this feature reads them.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForwardingReportRowDto {

    /** SaleOrderMaster.Id. Repeats across the three legs of the same order. */
    private Integer id;

    /** Which leg this row came from: 1, 2 or 3. */
    private Integer fwNo;

    private String cNumberDisplay;

    /** Sale date, pre-formatted dd/MM/yyyy for display. */
    private String saleDateDisplay;

    /** This leg's forwarding date, pre-formatted dd/MM/yyyy for display. */
    private String forwardingDateDisplay;

    /**
     * This leg's forwarding date as `yyyy-MM-dd HH:mm:ss`, empty when unset.
     *
     * <p>Kept alongside the display string because the row's red "overdue"
     * rule and the date editor both need a parseable value, and dd/MM/yyyy is
     * ambiguous to `Date.parse`.
     */
    private String forwardingDate;

    private String forwarding;
    private String forwardingEnterRef;
    private String forwardingExitRef;
    private String forwardingSmkNo;
    private String forwardingS1;
    private String forwardingS2;

    private Integer sealByRefId;
    private Integer sealBreakByRefId;
    private String sealByEmployee;
    private String breakSealEmployee;

    private String jobType;

    /**
     * The order is flagged as an original. Legacy painted these rows green and
     * gave that precedence over the overdue colouring.
     */
    private Boolean original;
}
