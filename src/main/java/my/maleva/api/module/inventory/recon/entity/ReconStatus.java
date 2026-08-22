package my.maleva.api.module.inventory.recon.entity;

/**
 * Lifecycle of one recon job.
 *
 * PENDING -> IN_PROGRESS -> COMPLETED
 *                        -> SCRAPPED
 *
 * Costs may only be added while the job is PENDING or IN_PROGRESS; once it has
 * completed the unit is already valued and back in stock, so a late cost line
 * would silently disagree with the stock value.
 */
public enum ReconStatus {

    /** Removed from the truck and sitting in the workshop, not yet sent. */
    PENDING,

    /** Being repaired - in Maleva's own bay, or away at a vendor. */
    IN_PROGRESS,

    /** Repaired and returned to stock as a RECON unit. */
    COMPLETED,

    /** Beyond repair. Written off; nothing returns to stock. */
    SCRAPPED
}
