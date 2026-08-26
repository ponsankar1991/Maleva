package my.maleva.api.module.inventory.service;

import my.maleva.api.module.inventory.dto.AvailableProductDto;
import my.maleva.api.module.inventory.dto.InventoryItemListDto;
import my.maleva.api.module.inventory.dto.InventoryItemRequestDto;
import my.maleva.api.module.inventory.dto.InventoryItemResponseDto;
import my.maleva.api.module.inventory.dto.InventoryTransactionDto;
import my.maleva.api.module.inventory.dto.ReceivePurchaseLineRequestDto;
import my.maleva.api.module.inventory.dto.UomOptionDto;
import my.maleva.api.module.inventory.entity.InventoryItem;

import java.util.List;

public interface InventoryItemService {

    /**
     * Create a catalogue item, its workshop settings and its opening balance in
     * one transaction. For serialised item types the first physical unit is
     * registered from firstSerialNo.
     *
     * Pass productRefId to reuse a product that already exists in ProductMaster;
     * leave it null to create a new product from itemCode and itemName.
     */
    InventoryItemResponseDto create(InventoryItemRequestDto request);

    /**
     * Return the store record for a product, creating a minimal one if the store
     * does not carry it yet.
     *
     * Receiving stock and cataloguing an item are separate acts everywhere else in
     * this module, which is correct when someone is setting the store up by hand.
     * A goods receipt has no such luxury: the parts are on the counter, and asking
     * the receiver to go and create the item first is how stock ends up recorded
     * against a product that no inventory screen will ever show. Nothing here
     * overwrites an existing row - a receipt is not the place to redefine an
     * item's type, bin or reorder level.
     *
     * @param itemType kind of stock, used only when creating; PART when null
     * @param unitCost price from the receipt, used only when creating
     * @param defaultSupplierRefId supplier the order was raised against, used
     *                             only when creating; left unset when null
     * @return the existing or newly created item
     */
    InventoryItem ensureStoreItem(Integer companyRefId, Integer productRefId, String itemType,
                                  Double unitCost, Integer defaultSupplierRefId, String modifiedBy);

    /**
     * As above, but the caller states the unit to catalogue a new item under.
     *
     * @param baseUomOverride UOM master id or unit name; null falls back to the
     *                        product's own UOM and then the type default
     */
    InventoryItem ensureStoreItem(Integer companyRefId, Integer productRefId, String itemType,
                                  Double unitCost, Integer defaultSupplierRefId, String modifiedBy,
                                  String baseUomOverride);

    /**
     * Receive a purchase order line: make sure the store carries the product, then
     * record the quantity against it, in one transaction.
     *
     * Lives here rather than on InventoryService because it needs both halves, and
     * this service already depends on that one - putting it the other way round
     * would make the two services depend on each other.
     */
    InventoryTransactionDto receivePurchaseLine(ReceivePurchaseLineRequestDto request);

    /**
     * Products that exist but are not yet set up for the workshop store - the
     * choices for the "existing product" path on the create screen.
     */
    List<AvailableProductDto> getAvailableProducts(Integer companyRefId);

    /**
     * Active units of measure for the company, for the new-product form's unit
     * picker. The chosen id becomes ProductMaster.UOM_Code.
     */
    List<UomOptionDto> getUomOptions(Integer companyRefId);

    /**
     * Update catalogue fields. Stock is never changed here - it only moves
     * through a recorded IN/OUT transaction.
     */
    InventoryItemResponseDto update(Integer id, InventoryItemRequestDto request);

    /** Full detail for one item, including balance and unit distribution. */
    InventoryItemResponseDto getById(Integer id);

    /** Detail looked up the way other modules reference stock: by product. */
    InventoryItemResponseDto getByProduct(Integer companyRefId, Integer productRefId);

    /**
     * List screen. itemType is optional (null = every type); search is optional
     * and matches item code, name, category, brand or a unit serial number.
     */
    List<InventoryItemListDto> search(Integer companyRefId, String itemType, String search);

    /** Quantity-tracked items at or below their reorder level. */
    List<InventoryItemListDto> getLowStock(Integer companyRefId);

    /** Soft delete - keeps the movement history intact. */
    boolean deactivate(Integer id, String modifiedBy);
}
