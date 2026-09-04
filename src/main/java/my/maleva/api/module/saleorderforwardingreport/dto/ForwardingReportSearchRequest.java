package my.maleva.api.module.saleorderforwardingreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Every filter the forwarding report screen can send.
 *
 * <p>Mirrors the legacy `SaleOrderReport` view model that
 * `POST /TransactionReport/SelectSaleOrderFWView` bound. The screen has three
 * forwarding legs and a filter per leg, so most fields come in threes: the
 * unsuffixed one applies to leg 1, `*2` to leg 2, `*3` to leg 3.
 *
 * <p>Two filters cut across all three legs and are deliberately not numbered:
 * {@code forwardingSmkNo} (the SMK box, matched against leg 1/2/3's own SMK
 * column) and {@code forwardingS1Search} (matched against each leg's S1).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForwardingReportSearchRequest {

    private Integer comId;

    /** Inclusive. Compared against each leg's own forwarding date column. */
    private String fromDate;

    /** Inclusive; the query widens it to `< toDate + 1 day` so times are kept. */
    private String toDate;

    /**
     * Job number. When set, legacy dropped every other filter — including the
     * date range — and matched on this alone. That behaviour is preserved.
     */
    private String cNumberDisplay;

    private String vesselName;

    /** Matched against leg 1, 2 and 3's SMK column at once. */
    private String forwardingSmkNo;

    /** Matched against leg 1, 2 and 3's S1 at once. */
    private String forwardingS1Search;

    private String forwardingSmkNo2;
    private String forwardingSmkNo3;

    /** K1 / K2 / K3 / K8, per leg. */
    private String forwarding;
    private String forwarding2;
    private String forwarding3;

    private Integer sealByRefId;
    private Integer sealBreakByRefId;
    private Integer sealByRefId2;
    private Integer sealBreakByRefId2;
    private Integer sealByRefId3;
    private Integer sealBreakByRefId3;

    private String forwarding1S1;
    private String forwarding1S2;
    private String forwarding2S1;
    private String forwarding2S2;
    private String forwarding3S1;
    private String forwarding3S2;

    private String forwardingEnterRef;
    private String forwardingExitRef;
    private String forwardingEnterRef2;
    private String forwardingExitRef2;
    private String forwardingEnterRef3;
    private String forwardingExitRef3;

    /* ─── ZB tab ──────────────────────────────────────────────────────── */

    private String zb;
    private String zb2;
    private String zbRef;
    private String zbRef2;
}
