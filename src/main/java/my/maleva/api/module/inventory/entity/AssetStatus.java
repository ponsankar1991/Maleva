package my.maleva.api.module.inventory.entity;

/**
 * Where one physical unit is right now.
 *
 * AVAILABLE -> INSTALLED -> AWAITING_RECON -> UNDER_REPAIR -> AVAILABLE ...
 * with SCRAPPED as the one terminal state.
 *
 * Only AVAILABLE units count as stock. A unit bolted to a truck, sitting on
 * the recon shelf or away at a vendor is not something the workshop can issue.
 */
public enum AssetStatus {

    /** On the shelf, ready to fit. Counts as stock. */
    AVAILABLE,

    /** Fitted to a truck. */
    INSTALLED,

    /**
     * Removed from a truck and held in the workshop, not yet sent anywhere.
     * Distinct from UNDER_REPAIR: this is the pile of cores waiting to be
     * assessed, and it is the worklist the store keeper works from.
     */
    AWAITING_RECON,

    /** Being repaired, either in Maleva's own workshop or at a vendor. */
    UNDER_REPAIR,

    /** Beyond repair and written off. Terminal - never returns to stock. */
    SCRAPPED
}
