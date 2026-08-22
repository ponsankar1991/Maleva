package my.maleva.api.module.inventory.recon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import my.maleva.api.module.inventory.recon.dto.*;
import my.maleva.api.module.inventory.recon.service.ReconService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reconditioning of repairable units.
 *
 * Follows the lifecycle: swap (removed from a truck) -> send (to the bay or a
 * vendor) -> costs -> complete (back in stock as recon) or scrap.
 */
@RestController
@RequestMapping("/api/inventory/recon")
@Tag(name = "Inventory Recon", description = "Recondition units removed from trucks")
public class ReconController {

    private static final Logger logger = LoggerFactory.getLogger(ReconController.class);

    @Autowired
    private ReconService reconService;

    @PostMapping("/swap")
    @Operation(summary = "Remove a failed unit from a truck and fit its replacement")
    public ResponseEntity<ReconJobDto> swap(@Valid @RequestBody ReconSwapRequestDto request) {
        logger.info("Recon swap: truck={}, removing={}",
                request.getTruckRefId(),
                request.getRemoved() == null ? null : request.getRemoved().getSerialNo());
        return new ResponseEntity<>(reconService.swap(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Send a pending unit for repair, in-house or to a vendor")
    public ResponseEntity<ReconJobDto> send(@PathVariable Integer id,
                                            @Valid @RequestBody SendForRepairRequestDto request) {
        return ResponseEntity.ok(reconService.sendForRepair(id, request));
    }

    @PostMapping("/{id}/costs")
    @Operation(summary = "Add a repair cost line")
    public ResponseEntity<ReconJobDto> addCost(@PathVariable Integer id,
                                               @Valid @RequestBody ReconCostRequestDto request) {
        return new ResponseEntity<>(reconService.addCost(id, request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/costs/{costId}")
    @Operation(summary = "Remove a repair cost line, reversing any stock it issued")
    public ResponseEntity<ReconJobDto> removeCost(@PathVariable Integer id,
                                                  @PathVariable Integer costId,
                                                  @RequestParam String modifiedBy) {
        return ResponseEntity.ok(reconService.removeCost(id, costId, modifiedBy));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Finish the repair and return the unit to stock as recon")
    public ResponseEntity<ReconJobDto> complete(@PathVariable Integer id,
                                                @Valid @RequestBody CompleteReconRequestDto request) {
        return ResponseEntity.ok(reconService.complete(id, request));
    }

    @PostMapping("/{id}/scrap")
    @Operation(summary = "Write off a unit that could not be repaired")
    public ResponseEntity<ReconJobDto> scrap(@PathVariable Integer id,
                                             @Valid @RequestBody ScrapReconRequestDto request) {
        return ResponseEntity.ok(reconService.scrap(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "One recon job with its cost lines")
    public ResponseEntity<ReconJobDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(reconService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Search recon jobs; every filter is optional")
    public ResponseEntity<List<ReconJobDto>> search(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer truckRefId,
            @RequestParam(required = false) Integer vendorRefId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(reconService.search(
                companyRefId, status, truckRefId, vendorRefId, fromDate, toDate));
    }

    @GetMapping("/open")
    @Operation(summary = "The recon shelf - units removed but not yet finished")
    public ResponseEntity<List<ReconJobDto>> open(@RequestParam Integer companyRefId) {
        return ResponseEntity.ok(reconService.getOpen(companyRefId));
    }

    @GetMapping("/truck/{truckRefId}")
    @Operation(summary = "Recon history for one truck")
    public ResponseEntity<List<ReconJobDto>> byTruck(@PathVariable Integer truckRefId,
                                                     @RequestParam Integer companyRefId) {
        return ResponseEntity.ok(reconService.getByTruck(companyRefId, truckRefId));
    }

    @GetMapping("/asset/{assetRefId}")
    @Operation(summary = "Every recon this individual unit has been through")
    public ResponseEntity<List<ReconJobDto>> byAsset(@PathVariable Integer assetRefId,
                                                     @RequestParam Integer companyRefId) {
        return ResponseEntity.ok(reconService.getByAsset(companyRefId, assetRefId));
    }

    @GetMapping("/summary/truck")
    @Operation(summary = "Recon spend grouped by truck")
    public ResponseEntity<List<TruckReconSummaryDto>> summaryByTruck(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(reconService.summariseByTruck(companyRefId, fromDate, toDate));
    }
}
