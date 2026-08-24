package my.maleva.api.module.inventory.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import my.maleva.api.module.inventory.dto.InventoryTransactionDto;
import my.maleva.api.module.inventory.dto.ReceivePurchaseLineRequestDto;
import my.maleva.api.module.inventory.dto.StockInRequestDto;
import my.maleva.api.module.inventory.dto.StockOutRequestDto;
import my.maleva.api.module.inventory.dto.TruckUsageDto;
import my.maleva.api.module.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stock movement endpoints: recording IN and OUT, and reading back what moved.
 * Failures are translated by InventoryExceptionHandler.
 * Base URL: /api/inventory
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private my.maleva.api.module.inventory.service.InventoryItemService inventoryItemService;

    /**
     * Record a stock receipt: opening balance, purchase, return or correction in.
     */
    @PostMapping("/stock-in")
    @PermitAll
    public ResponseEntity<InventoryTransactionDto> stockIn(@Valid @RequestBody StockInRequestDto request) {
        logger.info("Stock IN: company={}, product={}, qty={}",
                request.getCompanyRefId(), request.getProductRefId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.stockIn(request));
    }

    /**
     * Record a stock issue: job order consumption or correction out.
     * Returns 409 when the quantity exceeds what is on hand.
     */
    @PostMapping("/stock-out")
    @PermitAll
    public ResponseEntity<InventoryTransactionDto> stockOut(@Valid @RequestBody StockOutRequestDto request) {
        logger.info("Stock OUT: company={}, product={}, qty={}, truck={}",
                request.getCompanyRefId(), request.getProductRefId(),
                request.getQuantity(), request.getTruckRefId());
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.stockOut(request));
    }

    /**
     * Receive one purchase order line into the store.
     *
     * Creates the store record when the product is not carried yet, then records
     * the movement, both in one transaction. Two steps behind one call on purpose:
     * a receipt that catalogued the item and then failed to add the quantity would
     * leave an item reading zero that nobody can explain.
     */
    @PostMapping("/receive-purchase-line")
    @PermitAll
    public ResponseEntity<InventoryTransactionDto> receivePurchaseLine(
            @Valid @RequestBody ReceivePurchaseLineRequestDto request) {
        logger.info("Receive PO line: company={}, product={}, qty={}, po={}",
                request.getCompanyRefId(), request.getProductRefId(),
                request.getQuantity(), request.getPurchaseOrderRefId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryItemService.receivePurchaseLine(request));
    }

    /**
     * Current on-hand balance for one product in one company.
     */
    @GetMapping("/balance")
    @PermitAll
    public ResponseEntity<BigDecimal> getCurrentStock(
            @RequestParam Integer companyRefId,
            @RequestParam Integer productRefId) {
        return ResponseEntity.ok(inventoryService.getCurrentStock(companyRefId, productRefId));
    }

    /**
     * Full IN/OUT movement history for one product, newest first.
     */
    @GetMapping("/ledger")
    @PermitAll
    public ResponseEntity<List<InventoryTransactionDto>> getLedger(
            @RequestParam Integer companyRefId,
            @RequestParam Integer productRefId) {
        return ResponseEntity.ok(inventoryService.getLedger(companyRefId, productRefId));
    }

    /**
     * Consumption of one product broken down by truck: how much each truck took,
     * how often, its share of the total and when it last drew stock.
     */
    @GetMapping("/truck-usage")
    @PermitAll
    public ResponseEntity<List<TruckUsageDto>> getTruckUsage(
            @RequestParam Integer companyRefId,
            @RequestParam Integer productRefId) {
        return ResponseEntity.ok(inventoryService.getTruckUsage(companyRefId, productRefId));
    }
}
