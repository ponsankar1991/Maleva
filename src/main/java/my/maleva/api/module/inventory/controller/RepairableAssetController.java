package my.maleva.api.module.inventory.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import my.maleva.api.module.inventory.dto.*;
import my.maleva.api.module.inventory.service.RepairableAssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Serialised units: turbos, starters, compressors and workshop tools.
 * Each physical unit moves AVAILABLE -> INSTALLED -> UNDER_REPAIR -> AVAILABLE,
 * and can be refitted to a different truck each time it comes back from repair.
 * Failures are translated by InventoryExceptionHandler.
 * Base URL: /api/inventory/assets
 */
@RestController
@RequestMapping("/api/inventory/assets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RepairableAssetController {

    private static final Logger logger = LoggerFactory.getLogger(RepairableAssetController.class);

    @Autowired
    private RepairableAssetService assetService;

    /**
     * Register a new physical unit under an item.
     */
    @PostMapping("/register")
    @PermitAll
    public ResponseEntity<InventoryAssetDto> register(@Valid @RequestBody RegisterAssetRequestDto request) {
        logger.info("Registering unit: product={}, serial={}",
                request.getProductRefId(), request.getSerialNo());
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.registerAsset(request));
    }

    /**
     * Fit an available unit to a truck, or issue a tool to a technician.
     * Returns 409 if the unit is not currently available.
     */
    @PostMapping("/issue")
    @PermitAll
    public ResponseEntity<InventoryAssetDto> issue(@Valid @RequestBody IssueAssetRequestDto request) {
        logger.info("Issuing unit: serial={}, truck={}", request.getSerialNo(), request.getTruckRefId());
        return ResponseEntity.ok(assetService.issueAsset(request));
    }

    /**
     * Pull a failed unit off its truck and send it to the repair bay.
     * Returns 409 if the unit is not currently installed.
     */
    @PostMapping("/return-for-repair")
    @PermitAll
    public ResponseEntity<InventoryAssetDto> returnForRepair(
            @Valid @RequestBody ReturnForRepairRequestDto request) {
        logger.info("Returning unit for repair: serial={}", request.getSerialNo());
        return ResponseEntity.ok(assetService.returnForRepair(request));
    }

    /**
     * Repair finished - the unit is available again and can be fitted to any truck.
     * Returns 409 if the unit is not currently under repair.
     */
    @PostMapping("/mark-repaired")
    @PermitAll
    public ResponseEntity<InventoryAssetDto> markRepaired(
            @Valid @RequestBody MarkRepairedRequestDto request) {
        logger.info("Marking unit repaired: serial={}", request.getSerialNo());
        return ResponseEntity.ok(assetService.markRepaired(request));
    }

    /**
     * Every physical unit of an item, whatever its status.
     */
    @GetMapping
    @PermitAll
    public ResponseEntity<List<InventoryAssetDto>> getByProduct(
            @RequestParam Integer companyRefId,
            @RequestParam Integer productRefId) {
        return ResponseEntity.ok(assetService.getAssetsByProduct(companyRefId, productRefId));
    }

    /**
     * Units in one status across the company, e.g. everything UNDER_REPAIR.
     */
    @GetMapping("/status/{status}")
    @PermitAll
    public ResponseEntity<List<InventoryAssetDto>> getByStatus(
            @PathVariable String status,
            @RequestParam Integer companyRefId) {
        return ResponseEntity.ok(assetService.getAssetsByStatus(companyRefId, status));
    }

    /**
     * The life story of one unit: every truck it has been fitted to and why it came off.
     */
    @GetMapping("/{serialNo}/history")
    @PermitAll
    public ResponseEntity<List<InventoryTransactionDto>> getHistory(
            @PathVariable String serialNo,
            @RequestParam Integer companyRefId) {
        return ResponseEntity.ok(assetService.getAssetHistory(companyRefId, serialNo));
    }
}
