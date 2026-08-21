package my.maleva.api.module.inventory.service;

import my.maleva.api.module.inventory.dto.*;

import java.util.List;

public interface RepairableAssetService {

    /**
     * Register a new physical unit (a specific turbo, a specific tool) under a product,
     * with its own serial number. Adds 1 to the product's available stock.
     */
    InventoryAssetDto registerAsset(RegisterAssetRequestDto request);

    /**
     * Install an AVAILABLE unit onto a truck. Removes 1 from available stock and
     * flips the unit's status to INSTALLED.
     * Throws InvalidAssetStateException if the unit isn't AVAILABLE.
     */
    InventoryAssetDto issueAsset(IssueAssetRequestDto request);

    /**
     * Pull a broken unit off a truck for repair. Flips status INSTALLED -> UNDER_REPAIR.
     * Does not change available stock (it wasn't available while installed, and still isn't).
     * Throws InvalidAssetStateException if the unit isn't currently INSTALLED.
     */
    InventoryAssetDto returnForRepair(ReturnForRepairRequestDto request);

    /**
     * Repair finished — the unit is usable again. Flips UNDER_REPAIR -> AVAILABLE and
     * adds 1 back to available stock, ready to be issued to another truck.
     * Throws InvalidAssetStateException if the unit isn't currently UNDER_REPAIR.
     */
    InventoryAssetDto markRepaired(MarkRepairedRequestDto request);

    /**
     * All physical units of a product in a company, whatever their status.
     */
    List<InventoryAssetDto> getAssetsByProduct(Integer companyRefId, Integer productRefId);

    /**
     * All physical units of a product in a company in a given status (e.g. all UNDER_REPAIR).
     */
    List<InventoryAssetDto> getAssetsByStatus(Integer companyRefId, String status);

    /**
     * Full life story of one physical unit: which trucks it's been on and why, newest first.
     */
    List<InventoryTransactionDto> getAssetHistory(Integer companyRefId, String serialNo);
}
