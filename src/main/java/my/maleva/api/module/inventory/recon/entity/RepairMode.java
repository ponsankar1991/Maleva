package my.maleva.api.module.inventory.recon.entity;

/**
 * Who does the repair. VENDOR requires a supplier on the job, IN_HOUSE forbids
 * one - enforced both here in the service and by CK_Recon_Vendor in the table.
 */
public enum RepairMode {

    /** Repaired by Maleva's own workshop. Cost is labour plus parts issued. */
    IN_HOUSE,

    /** Sent out to a supplier. Cost is their invoice, plus any transport. */
    VENDOR
}
