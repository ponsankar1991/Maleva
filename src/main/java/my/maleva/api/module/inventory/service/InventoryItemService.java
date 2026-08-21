package my.maleva.api.module.inventory.service;

import my.maleva.api.module.inventory.dto.AvailableProductDto;
import my.maleva.api.module.inventory.dto.InventoryItemListDto;
import my.maleva.api.module.inventory.dto.InventoryItemRequestDto;
import my.maleva.api.module.inventory.dto.InventoryItemResponseDto;
import my.maleva.api.module.inventory.dto.UomOptionDto;

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
