package my.maleva.api.module.inventory.recon.service;

import my.maleva.api.module.inventory.recon.dto.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reconditioning of repairable units.
 *
 * The lifecycle a unit goes through here:
 *
 *   swap()      removed from a truck, replacement fitted   -> PENDING
 *   send()      handed to the workshop bay or a vendor     -> IN_PROGRESS
 *   addCost()   labour, parts, vendor invoice, transport
 *   complete()  repaired, back on the shelf as RECON stock -> COMPLETED
 *   scrap()     beyond repair, written off                 -> SCRAPPED
 */
public interface ReconService {

    /** Remove a failed unit from a truck and fit its replacement, atomically. */
    ReconJobDto swap(ReconSwapRequestDto request);

    /** Send a pending job for repair, in-house or to a vendor. */
    ReconJobDto sendForRepair(Integer reconId, SendForRepairRequestDto request);

    /** Add a line of repair spend; issues stock when the line is a store part. */
    ReconJobDto addCost(Integer reconId, ReconCostRequestDto request);

    /** Remove a cost line, reversing any stock it issued. */
    ReconJobDto removeCost(Integer reconId, Integer costId, String modifiedBy);

    /** Finish the repair: the unit returns to stock valued at the repair spend. */
    ReconJobDto complete(Integer reconId, CompleteReconRequestDto request);

    /** Write the unit off. Nothing returns to stock. */
    ReconJobDto scrap(Integer reconId, ScrapReconRequestDto request);

    ReconJobDto getById(Integer reconId);

    List<ReconJobDto> search(Integer companyRefId, String status, Integer truckRefId,
                             Integer vendorRefId, LocalDateTime fromDate, LocalDateTime toDate);

    /** Everything removed but not yet finished - the recon shelf worklist. */
    List<ReconJobDto> getOpen(Integer companyRefId);

    /** Recon history for one truck. */
    List<ReconJobDto> getByTruck(Integer companyRefId, Integer truckRefId);

    /** Every recon this individual unit has been through. */
    List<ReconJobDto> getByAsset(Integer companyRefId, Integer assetRefId);

    /** Recon spend grouped by truck. */
    List<TruckReconSummaryDto> summariseByTruck(Integer companyRefId,
                                                LocalDateTime fromDate, LocalDateTime toDate);
}
