package my.maleva.api.module.inventory.recon.entity;

/**
 * What one line of repair spend is for.
 *
 * The type decides which total on the job header the line rolls into, and PART
 * additionally issues stock, because a part fitted during a recon has left the
 * workshop store.
 */
public enum ReconCostType {

    /** Workshop labour. Rolls into labourCost. */
    LABOUR,

    /**
     * A part consumed during the repair. Rolls into partsCost, and when it
     * names a productRefId the service issues that quantity out of store.
     */
    PART,

    /** An outside repairer's invoice. Rolls into vendorCost. */
    VENDOR_INVOICE,

    /** Sending the unit out and getting it back. Rolls into otherCost. */
    TRANSPORT,

    /** Anything else. Rolls into otherCost. */
    OTHER
}
