package my.maleva.api.module.inventory.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import my.maleva.api.module.inventory.dto.AvailableProductDto;
import my.maleva.api.module.inventory.dto.InventoryItemListDto;
import my.maleva.api.module.inventory.dto.InventoryItemRequestDto;
import my.maleva.api.module.inventory.dto.InventoryItemResponseDto;
import my.maleva.api.module.inventory.service.InventoryItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Inventory item catalogue: insert, update and select.
 * Base URL: /api/inventory/items
 */
@RestController
@RequestMapping("/api/inventory/items")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryItemController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryItemController.class);

    @Autowired
    private InventoryItemService itemService;

    /**
     * List / search items for a company.
     * itemType and search are both optional; search matches item code, name,
     * category, brand or the serial number of any of the item's units.
     */
    @GetMapping
    @PermitAll
    public ResponseEntity<List<InventoryItemListDto>> search(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String search) {
        logger.info("Listing inventory items: company={}, type={}, search={}",
                companyRefId, itemType, search);
        return ResponseEntity.ok(itemService.search(companyRefId, itemType, search));
    }

    /**
     * Items at or below their reorder level.
     */
    @GetMapping("/low-stock")
    @PermitAll
    public ResponseEntity<List<InventoryItemListDto>> lowStock(@RequestParam Integer companyRefId) {
        return ResponseEntity.ok(itemService.getLowStock(companyRefId));
    }

    /**
     * Products that exist in ProductMaster but are not yet set up for the
     * workshop store. Use these to add an existing product to inventory rather
     * than creating a second product for the same physical thing.
     */
    @GetMapping("/available-products")
    @PermitAll
    public ResponseEntity<List<AvailableProductDto>> availableProducts(
            @RequestParam Integer companyRefId) {
        return ResponseEntity.ok(itemService.getAvailableProducts(companyRefId));
    }

    /**
     * Full detail for one item.
     */
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<InventoryItemResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.getById(id));
    }

    /**
     * Detail by product, for callers that already hold a ProductMaster id.
     */
    @GetMapping("/by-product/{productRefId}")
    @PermitAll
    public ResponseEntity<InventoryItemResponseDto> getByProduct(
            @PathVariable Integer productRefId,
            @RequestParam Integer companyRefId) {
        return ResponseEntity.ok(itemService.getByProduct(companyRefId, productRefId));
    }

    /**
     * Insert a new item, with its opening balance or first serialised unit.
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<InventoryItemResponseDto> create(
            @Valid @RequestBody InventoryItemRequestDto request) {
        logger.info("Creating inventory item: company={}, code={}",
                request.getCompanyRefId(), request.getItemCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(request));
    }

    /**
     * Update catalogue fields. Stock is not changed here - use the stock-in
     * and stock-out endpoints so every change leaves a movement record.
     */
    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<InventoryItemResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody InventoryItemRequestDto request) {
        logger.info("Updating inventory item: id={}", id);
        return ResponseEntity.ok(itemService.update(id, request));
    }

    /**
     * Soft delete. The item stops appearing in lists but its movement history
     * and any serialised units are preserved.
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<Void> deactivate(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "SYSTEM") String modifiedBy) {
        logger.info("Deactivating inventory item: id={}", id);
        return itemService.deactivate(id, modifiedBy)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
