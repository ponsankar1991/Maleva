package my.maleva.api.module.inventory.entity;

/**
 * Whether a unit is factory new or has been through the workshop.
 *
 * A unit is born NEW. The first completed recon flips it to RECON for good;
 * InventoryAsset.reconCount records how many times it has been round.
 * This is what the stock screens mean by "Recond".
 */
public enum AssetCondition {
    NEW,
    RECON
}
