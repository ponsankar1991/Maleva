package my.maleva.api.module.inventory.entity;

/**
 * The four kinds of stock the workshop holds. The distinction that matters:
 * CONSUMABLE and PART are tracked by quantity, ASSET and TOOL are tracked
 * by individual serial number through InventoryAsset.
 */
public enum ItemType {

    CONSUMABLE(false),
    PART(false),
    ASSET(true),
    TOOL(true);

    private final boolean serialised;

    ItemType(boolean serialised) {
        this.serialised = serialised;
    }

    public boolean isSerialised() {
        return serialised;
    }
}
