package my.maleva.api.module.inventory.recon.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.inventory.dto.IssueAssetRequestDto;
import my.maleva.api.module.inventory.dto.StockInRequestDto;
import my.maleva.api.module.inventory.dto.StockOutRequestDto;
import my.maleva.api.module.inventory.entity.AssetCondition;
import my.maleva.api.module.inventory.entity.AssetStatus;
import my.maleva.api.module.inventory.entity.InventoryAsset;
import my.maleva.api.module.inventory.exception.InvalidAssetStateException;
import my.maleva.api.module.inventory.recon.dto.*;
import my.maleva.api.module.inventory.recon.entity.*;
import my.maleva.api.module.inventory.recon.repository.ReconCostRepository;
import my.maleva.api.module.inventory.recon.repository.ReconJobRepository;
import my.maleva.api.module.inventory.recon.service.ReconService;
import my.maleva.api.module.inventory.repository.InventoryAssetRepository;
import my.maleva.api.module.inventory.service.InventoryService;
import my.maleva.api.module.inventory.service.RepairableAssetService;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reconditioning of repairable units.
 *
 * The rule that shapes everything here: a unit removed from a truck is NOT
 * stock. It is a dead core sitting in the workshop. It only becomes stock
 * again at complete(), and it re-enters valued at what the repair cost - the
 * core itself is carried at zero, having been expensed when it was first
 * fitted. That is what makes "recon RM 1,000" and "new RM 7,500" comparable
 * figures rather than two numbers meaning different things.
 */
@Service
@Transactional
public class ReconServiceImpl implements ReconService {

    private static final Logger logger = LoggerFactory.getLogger(ReconServiceImpl.class);

    /** Matches the SequenceNoMaster row seeded by db/table/inventoryrecon.sql. */
    private static final String SEQUENCE_NAME = "ReconJob";
    private static final String RECON_PREFIX = "RCN";
    private static final int SEQUENCE_PADDING = 9;

    /** Reference types written onto the stock ledger by this service. */
    private static final String REF_RECON_SWAP = "RECON_SWAP";
    private static final String REF_RECON_JOB = "RECON_JOB";
    private static final String REF_RECON_COMPLETE = "RECON_COMPLETE";

    @Autowired private ReconJobRepository reconRepository;
    @Autowired private ReconCostRepository costRepository;
    @Autowired private InventoryAssetRepository assetRepository;
    @Autowired private TruckMasterRepository truckRepository;
    @Autowired private SequenceNoMasterRepository sequenceRepository;
    @Autowired private InventoryService inventoryService;
    @Autowired private RepairableAssetService repairableAssetService;

    // ==================================================================
    // 1. Swap - remove the failed unit, fit the replacement
    // ==================================================================

    @Override
    @Transactional
    public ReconJobDto swap(ReconSwapRequestDto request) {
        if (!truckRepository.existsById(request.getTruckRefId())) {
            throw new EntityNotFoundException("Truck not found with ID: " + request.getTruckRefId());
        }

        ReconSwapRequestDto.RemovedUnit removed = request.getRemoved();
        String removedSerial = removed.getSerialNo().trim();

        // ---- remove leg -------------------------------------------------
        InventoryAsset old = lockAsset(request.getCompanyRefId(), removed.getProductRefId(), removedSerial);

        if (old.getStatus() != AssetStatus.INSTALLED) {
            // Says what to do next, not only what is wrong. The commonest case by
            // far is a unit still on the shelf, where the missing step is fitting
            // it to a truck - naming that is the difference between a message the
            // storekeeper can act on and one they have to ask about.
            String next = switch (old.getStatus()) {
                case AVAILABLE -> "It is on the shelf. Issue it to a truck first, then it can be removed.";
                case AWAITING_RECON -> "It has already been removed and is waiting on the recon shelf.";
                case UNDER_REPAIR -> "It is already away being repaired.";
                case SCRAPPED -> "It has been scrapped and cannot be used again.";
                default -> "";
            };
            throw new InvalidAssetStateException("Serial No '" + removedSerial
                    + "' cannot be removed - it is not fitted to a truck (status: "
                    + old.getStatus() + "). " + next);
        }
        // Guards against recording a removal from the wrong truck, which would
        // send the repair bill to a truck that never had the part.
        if (!Objects.equals(old.getCurrentTruckRefId(), request.getTruckRefId())) {
            throw new InvalidAssetStateException("Serial No '" + removedSerial
                    + "' is fitted on truck " + old.getCurrentTruckRefId()
                    + ", not truck " + request.getTruckRefId());
        }
        reconRepository.findOpenByAsset(request.getCompanyRefId(), old.getId()).ifPresent(open -> {
            throw new InvalidRequestException("Serial No '" + removedSerial
                    + "' is already on open recon job " + open.getReconNo()
                    + ". Close that job before removing the unit again.");
        });

        // LastTruckRefId is set before CurrentTruckRefId is cleared - otherwise
        // the truck the unit came off is gone the moment it reaches the shelf.
        old.setLastTruckRefId(old.getCurrentTruckRefId());
        old.setCurrentTruckRefId(null);
        old.setStatus(AssetStatus.AWAITING_RECON);
        old.setModifiedBy(request.getPerformedBy());
        assetRepository.save(old);

        // No stock-in here on purpose. A failed core is not issuable stock; it
        // becomes stock again only when the repair completes.

        // ---- the recon job ----------------------------------------------
        ReconJob job = reconRepository.save(ReconJob.builder()
                .companyRefId(request.getCompanyRefId())
                .reconNo(nextReconNo(request.getCompanyRefId()))
                .productRefId(removed.getProductRefId())
                .assetRefId(old.getId())
                .serialNo(removedSerial)
                .removedFromTruckRefId(request.getTruckRefId())
                .removedOnJobOrderRefId(request.getJobOrderRefId())
                .removedDate(LocalDateTime.now())
                .removedBy(request.getPerformedBy())
                .faultDescription(trimOrNull(removed.getFaultDescription()))
                .newPartCost(removed.getNewPartCost())
                .status(ReconStatus.PENDING)
                .labourCost(BigDecimal.ZERO)
                .partsCost(BigDecimal.ZERO)
                .vendorCost(BigDecimal.ZERO)
                .otherCost(BigDecimal.ZERO)
                .remarks(trimOrNull(request.getRemarks()))
                .active(1)
                .modifiedBy(request.getPerformedBy())
                .build());

        // ---- fit leg (optional) -----------------------------------------
        if (request.getFitted() != null) {
            ReconSwapRequestDto.FittedUnit fitted = request.getFitted();
            String fittedSerial = fitted.getSerialNo().trim();

            if (fitted.getProductRefId().equals(removed.getProductRefId())
                    && fittedSerial.equalsIgnoreCase(removedSerial)) {
                throw new InvalidRequestException(
                        "The removed unit and the fitted unit cannot be the same serial number");
            }

            InventoryAsset replacement = assetRepository
                    .findByCompanyRefIdAndProductRefIdAndSerialNo(
                            request.getCompanyRefId(), fitted.getProductRefId(), fittedSerial)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Serial No '" + fittedSerial + "' not found for the fitted product"));

            // Reuses the existing issue path, so the stock ledger entry for a
            // replacement looks the same as any other issue to a truck.
            repairableAssetService.issueAsset(IssueAssetRequestDto.builder()
                    .companyRefId(request.getCompanyRefId())
                    .productRefId(fitted.getProductRefId())
                    .serialNo(fittedSerial)
                    .truckRefId(request.getTruckRefId())
                    .jobOrderRefId(request.getJobOrderRefId())
                    .remarks("Replaces " + removedSerial + " (" + job.getReconNo() + ")")
                    .createdBy(request.getPerformedBy())
                    .build());

            job.setReplacedByProductRefId(fitted.getProductRefId());
            job.setReplacedBySerialNo(fittedSerial);
            job.setReplacedByCondition(replacement.getCondition() == null
                    ? AssetCondition.NEW.name()
                    : replacement.getCondition().name());
            reconRepository.save(job);
        }

        logger.info("Recon swap: {} removed {} from truck {}, fitted {}",
                job.getReconNo(), removedSerial, request.getTruckRefId(),
                job.getReplacedBySerialNo() == null ? "nothing" : job.getReplacedBySerialNo());

        return toDto(job, costsOf(job.getId()));
    }

    // ==================================================================
    // 2. Send for repair - in-house or vendor
    // ==================================================================

    @Override
    @Transactional
    public ReconJobDto sendForRepair(Integer reconId, SendForRepairRequestDto request) {
        ReconJob job = lockJob(reconId);
        requireStatus(job, ReconStatus.PENDING);

        RepairMode mode = parseRepairMode(request.getRepairMode());

        // Mirrors CK_Recon_Vendor on the table, but fails with a sentence the
        // storekeeper can act on rather than a constraint violation.
        if (mode == RepairMode.VENDOR && request.getVendorRefId() == null) {
            throw new InvalidRequestException(
                    "A vendor is required when the unit is repaired outside. "
                    + "Pick the vendor, or choose IN_HOUSE if your own workshop is repairing it.");
        }
        if (mode == RepairMode.IN_HOUSE && request.getVendorRefId() != null) {
            throw new InvalidRequestException(
                    "An in-house repair cannot have a vendor. Choose VENDOR to send it outside.");
        }

        job.setRepairMode(mode);
        job.setVendorRefId(request.getVendorRefId());
        job.setVendorDocNo(trimOrNull(request.getVendorDocNo()));
        job.setSentDate(LocalDateTime.now());
        job.setExpectedDate(request.getExpectedDate());
        job.setStatus(ReconStatus.IN_PROGRESS);
        job.setModifiedBy(request.getModifiedBy());
        appendRemark(job, request.getRemarks());
        reconRepository.save(job);

        InventoryAsset asset = loadAsset(job);
        asset.setStatus(AssetStatus.UNDER_REPAIR);
        asset.setModifiedBy(request.getModifiedBy());
        assetRepository.save(asset);

        logger.info("Recon {} sent for repair: mode={}, vendor={}",
                job.getReconNo(), mode, request.getVendorRefId());

        return toDto(job, costsOf(job.getId()));
    }

    // ==================================================================
    // 3. Repair cost
    // ==================================================================

    @Override
    @Transactional
    public ReconJobDto addCost(Integer reconId, ReconCostRequestDto request) {
        ReconJob job = lockJob(reconId);
        // Costs are closed once the unit is valued and back in stock; a later
        // line would leave the stock value disagreeing with the job total.
        requireStatus(job, ReconStatus.PENDING, ReconStatus.IN_PROGRESS);

        ReconCostType type = parseCostType(request.getCostType());
        BigDecimal amount = request.getQuantity().multiply(request.getRate())
                .setScale(2, RoundingMode.HALF_UP);

        ReconCost line = costRepository.save(ReconCost.builder()
                .reconRefId(job.getId())
                .costType(type)
                .description(trimOrNull(request.getDescription()))
                .productRefId(request.getProductRefId())
                .quantity(request.getQuantity())
                .rate(request.getRate())
                .amount(amount)
                .supplierRefId(request.getSupplierRefId())
                .docNo(trimOrNull(request.getDocNo()))
                .docDate(request.getDocDate())
                .remarks(trimOrNull(request.getRemarks()))
                .createdBy(request.getCreatedBy())
                .build());

        // A part fitted during the repair has left the store. Without this the
        // balance keeps counting stock that is already inside the recon unit.
        if (type == ReconCostType.PART && request.getProductRefId() != null) {
            inventoryService.stockOut(StockOutRequestDto.builder()
                    .companyRefId(job.getCompanyRefId())
                    .productRefId(request.getProductRefId())
                    .quantity(request.getQuantity())
                    .referenceType(REF_RECON_JOB)
                    .referenceId(job.getId())
                    .remarks("Recon " + job.getReconNo() + " - " + trimOrNull(request.getDescription()))
                    .createdBy(request.getCreatedBy())
                    .build());
        }

        recalculateTotals(job, request.getCreatedBy());

        logger.info("Recon {} cost added: type={}, amount={}", job.getReconNo(), type, amount);

        return toDto(job, costsOf(job.getId()));
    }

    @Override
    @Transactional
    public ReconJobDto removeCost(Integer reconId, Integer costId, String modifiedBy) {
        ReconJob job = lockJob(reconId);
        requireStatus(job, ReconStatus.PENDING, ReconStatus.IN_PROGRESS);

        ReconCost line = costRepository.findById(costId)
                .orElseThrow(() -> new EntityNotFoundException("Cost line not found with ID: " + costId));
        if (!Objects.equals(line.getReconRefId(), job.getId())) {
            throw new InvalidRequestException(
                    "Cost line " + costId + " does not belong to recon " + job.getReconNo());
        }

        // Put back whatever the line took out, so deleting a line is a true
        // reversal rather than a quiet loss of stock.
        if (line.getCostType() == ReconCostType.PART && line.getProductRefId() != null) {
            inventoryService.stockIn(StockInRequestDto.builder()
                    .companyRefId(job.getCompanyRefId())
                    .productRefId(line.getProductRefId())
                    .quantity(line.getQuantity())
                    .referenceType(REF_RECON_JOB)
                    .referenceId(job.getId())
                    .remarks("Reversal of recon " + job.getReconNo() + " cost line " + costId)
                    .createdBy(modifiedBy)
                    .build());
        }

        costRepository.delete(line);
        recalculateTotals(job, modifiedBy);

        logger.info("Recon {} cost line {} removed", job.getReconNo(), costId);

        return toDto(job, costsOf(job.getId()));
    }

    // ==================================================================
    // 4. Complete - back on the shelf as recon stock
    // ==================================================================

    @Override
    @Transactional
    public ReconJobDto complete(Integer reconId, CompleteReconRequestDto request) {
        ReconJob job = lockJob(reconId);
        requireStatus(job, ReconStatus.IN_PROGRESS);

        BigDecimal repairSpend = totalOf(job);

        job.setResultingUnitCost(repairSpend);
        job.setReceivedDate(request.getReceivedDate() == null
                ? LocalDateTime.now() : request.getReceivedDate());
        job.setStatus(ReconStatus.COMPLETED);
        job.setModifiedBy(request.getModifiedBy());
        appendRemark(job, request.getRemarks());
        reconRepository.save(job);

        InventoryAsset asset = loadAsset(job);
        asset.setStatus(AssetStatus.AVAILABLE);
        // Permanent from the first recon onward - the unit is no longer new.
        asset.setCondition(AssetCondition.RECON);
        asset.setReconCount((asset.getReconCount() == null ? 0 : asset.getReconCount()) + 1);
        asset.setCurrentValue(repairSpend);
        asset.setModifiedBy(request.getModifiedBy());
        assetRepository.save(asset);

        inventoryService.stockIn(StockInRequestDto.builder()
                .companyRefId(job.getCompanyRefId())
                .productRefId(job.getProductRefId())
                .quantity(BigDecimal.ONE)
                .referenceType(REF_RECON_COMPLETE)
                .referenceId(job.getId())
                .assetSerialNo(job.getSerialNo())
                .remarks("Recon " + job.getReconNo() + " complete, unit valued at " + repairSpend)
                .createdBy(request.getModifiedBy())
                .build());

        logger.info("Recon {} complete: serial={} back in stock as RECON at {}",
                job.getReconNo(), job.getSerialNo(), repairSpend);

        return toDto(job, costsOf(job.getId()));
    }

    // ==================================================================
    // 5. Scrap - written off
    // ==================================================================

    @Override
    @Transactional
    public ReconJobDto scrap(Integer reconId, ScrapReconRequestDto request) {
        ReconJob job = lockJob(reconId);
        requireStatus(job, ReconStatus.PENDING, ReconStatus.IN_PROGRESS);

        // Deliberately no stock-in: a scrapped unit never becomes stock again.
        // Whatever was already spent on it stays on the job as the write-off.
        job.setResultingUnitCost(BigDecimal.ZERO);
        job.setStatus(ReconStatus.SCRAPPED);
        job.setModifiedBy(request.getModifiedBy());
        appendRemark(job, "Scrapped: " + request.getReason());
        reconRepository.save(job);

        InventoryAsset asset = loadAsset(job);
        asset.setStatus(AssetStatus.SCRAPPED);
        asset.setCurrentValue(BigDecimal.ZERO);
        asset.setModifiedBy(request.getModifiedBy());
        assetRepository.save(asset);

        logger.info("Recon {} scrapped: serial={}, written off {}",
                job.getReconNo(), job.getSerialNo(), totalOf(job));

        return toDto(job, costsOf(job.getId()));
    }

    // ==================================================================
    // Reads
    // ==================================================================

    @Override
    @Transactional(readOnly = true)
    public ReconJobDto getById(Integer reconId) {
        ReconJob job = reconRepository.findById(reconId)
                .orElseThrow(() -> new EntityNotFoundException("Recon job not found with ID: " + reconId));
        return toDto(job, costsOf(job.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconJobDto> search(Integer companyRefId, String status, Integer truckRefId,
                                    Integer vendorRefId, LocalDateTime fromDate, LocalDateTime toDate) {
        ReconStatus parsed = (status == null || status.trim().isEmpty())
                ? null : parseStatus(status);
        return withCosts(reconRepository.search(
                companyRefId, parsed, truckRefId, vendorRefId, fromDate, toDate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconJobDto> getOpen(Integer companyRefId) {
        return withCosts(reconRepository.findOpen(companyRefId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconJobDto> getByTruck(Integer companyRefId, Integer truckRefId) {
        return withCosts(reconRepository
                .findByCompanyRefIdAndRemovedFromTruckRefIdAndActiveOrderByRemovedDateDesc(
                        companyRefId, truckRefId, 1));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReconJobDto> getByAsset(Integer companyRefId, Integer assetRefId) {
        return withCosts(reconRepository
                .findByCompanyRefIdAndAssetRefIdAndActiveOrderByRemovedDateDesc(
                        companyRefId, assetRefId, 1));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TruckReconSummaryDto> summariseByTruck(Integer companyRefId,
                                                       LocalDateTime fromDate, LocalDateTime toDate) {
        List<Object[]> rows = reconRepository.summariseByTruck(companyRefId, fromDate, toDate);

        List<Integer> truckIds = rows.stream()
                .map(r -> (Integer) r[0])
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // One lookup for every truck in the result rather than one per row.
        Map<Integer, String> names = truckIds.isEmpty()
                ? Map.of()
                : truckRepository.findAllById(truckIds).stream()
                    .filter(t -> t.getId() != null)
                    .collect(Collectors.toMap(t -> t.getId(), t -> String.valueOf(t.getTruckName()),
                            (a, b) -> a));

        return rows.stream()
                .map(r -> TruckReconSummaryDto.builder()
                        .truckRefId((Integer) r[0])
                        .truckName(names.get((Integer) r[0]))
                        .jobCount(((Number) r[1]).longValue())
                        .totalCost(r[2] == null ? BigDecimal.ZERO : (BigDecimal) r[2])
                        .build())
                .collect(Collectors.toList());
    }

    // ==================================================================
    // Internals
    // ==================================================================

    /**
     * Re-sums the cost lines onto the job header. The header totals are
     * denormalised so list and report screens do not aggregate the child table
     * per row; this is the single place that keeps them true.
     */
    private void recalculateTotals(ReconJob job, String modifiedBy) {
        BigDecimal labour = BigDecimal.ZERO;
        BigDecimal parts = BigDecimal.ZERO;
        BigDecimal vendor = BigDecimal.ZERO;
        BigDecimal other = BigDecimal.ZERO;

        for (ReconCost line : costRepository.findByReconRefIdOrderByIdAsc(job.getId())) {
            BigDecimal amount = line.getAmount() == null ? BigDecimal.ZERO : line.getAmount();
            switch (line.getCostType()) {
                case LABOUR -> labour = labour.add(amount);
                case PART -> parts = parts.add(amount);
                case VENDOR_INVOICE -> vendor = vendor.add(amount);
                default -> other = other.add(amount);
            }
        }

        job.setLabourCost(labour);
        job.setPartsCost(parts);
        job.setVendorCost(vendor);
        job.setOtherCost(other);
        job.setModifiedBy(modifiedBy);
        reconRepository.save(job);
    }

    /**
     * TotalCost is a computed column, so a job just written in this same
     * transaction has a stale or null value for it. Summing the four parts is
     * always correct and never needs a re-read.
     */
    private static BigDecimal totalOf(ReconJob job) {
        return zero(job.getLabourCost())
                .add(zero(job.getPartsCost()))
                .add(zero(job.getVendorCost()))
                .add(zero(job.getOtherCost()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Allocates the next recon number the same way PL and BO numbers are. */
    private String nextReconNo(Integer companyRefId) {
        Integer currentMax = sequenceRepository
                .findMaxSequenceNoByCompanyAndSequenceName(companyRefId, SEQUENCE_NAME);
        int next = (currentMax == null ? 0 : currentMax) + 1;

        LocalDateTime now = LocalDateTime.now();
        SequenceNoMaster row = sequenceRepository
                .findByCompanyRefIdAndSequenceName(companyRefId, SEQUENCE_NAME)
                .orElseGet(() -> SequenceNoMaster.builder()
                        .companyRefId(companyRefId)
                        .sequenceName(SEQUENCE_NAME)
                        .build());
        row.setSequenceNo(next);
        row.setSequenceDate(now);
        row.setSequenceYear(now.getYear());
        row.setSequenceMonth(now.getMonthValue());
        sequenceRepository.save(row);

        return String.format("%s%0" + SEQUENCE_PADDING + "d", RECON_PREFIX, next);
    }

    private ReconJob lockJob(Integer reconId) {
        ReconJob job = reconRepository.lockById(reconId)
                .orElseThrow(() -> new EntityNotFoundException("Recon job not found with ID: " + reconId));
        if (job.getActive() != null && job.getActive() == 0) {
            throw new InvalidRequestException("Recon job " + job.getReconNo() + " is not active");
        }
        return job;
    }

    private InventoryAsset lockAsset(Integer companyRefId, Integer productRefId, String serialNo) {
        return assetRepository.lockBySerial(companyRefId, productRefId, serialNo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Serial No '" + serialNo + "' not found for this product"));
    }

    private InventoryAsset loadAsset(ReconJob job) {
        return assetRepository.findById(job.getAssetRefId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Unit not found for recon " + job.getReconNo()
                                + " (asset id " + job.getAssetRefId() + ")"));
    }

    private static void requireStatus(ReconJob job, ReconStatus... allowed) {
        for (ReconStatus status : allowed) {
            if (job.getStatus() == status) {
                return;
            }
        }
        throw new InvalidAssetStateException("Recon " + job.getReconNo() + " is "
                + job.getStatus() + "; this action needs it to be "
                + Arrays.stream(allowed).map(Enum::name).collect(Collectors.joining(" or ")));
    }

    private static void appendRemark(ReconJob job, String addition) {
        String trimmed = trimOrNull(addition);
        if (trimmed == null) {
            return;
        }
        String existing = job.getRemarks();
        String combined = (existing == null || existing.isBlank())
                ? trimmed
                : existing + " | " + trimmed;
        // Remarks is VARCHAR(500); truncate rather than fail a repair over it.
        job.setRemarks(combined.length() > 500 ? combined.substring(0, 500) : combined);
    }

    private List<ReconCost> costsOf(Integer reconId) {
        return costRepository.findByReconRefIdOrderByIdAsc(reconId);
    }

    /** Loads every job's cost lines in one query instead of one query per job. */
    private List<ReconJobDto> withCosts(List<ReconJob> jobs) {
        if (jobs.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = jobs.stream().map(ReconJob::getId).collect(Collectors.toList());
        Map<Integer, List<ReconCost>> byJob = costRepository.findByReconRefIdIn(ids).stream()
                .collect(Collectors.groupingBy(ReconCost::getReconRefId));

        return jobs.stream()
                .map(job -> toDto(job, byJob.getOrDefault(job.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private ReconJobDto toDto(ReconJob job, List<ReconCost> costs) {
        BigDecimal total = totalOf(job);
        BigDecimal saving = job.getNewPartCost() == null
                ? null
                : job.getNewPartCost().subtract(total).setScale(2, RoundingMode.HALF_UP);

        // Finished jobs report how long the repair took; open ones report how
        // long the unit has been out of service so far.
        LocalDateTime end = job.getReceivedDate() == null ? LocalDateTime.now() : job.getReceivedDate();
        Integer days = job.getRemovedDate() == null
                ? null
                : (int) Duration.between(job.getRemovedDate(), end).toDays();

        InventoryAsset asset = job.getAsset();

        return ReconJobDto.builder()
                .id(job.getId())
                .companyRefId(job.getCompanyRefId())
                .reconNo(job.getReconNo())
                .productRefId(job.getProductRefId())
                .productCode(job.getProductMaster() == null ? null : job.getProductMaster().getProdCode())
                .productName(job.getProductMaster() == null ? null : job.getProductMaster().getPname())
                .assetRefId(job.getAssetRefId())
                .serialNo(job.getSerialNo())
                .removedFromTruckRefId(job.getRemovedFromTruckRefId())
                .removedFromTruckName(job.getRemovedFromTruck() == null
                        ? null : job.getRemovedFromTruck().getTruckName())
                .removedOnJobOrderRefId(job.getRemovedOnJobOrderRefId())
                .removedDate(job.getRemovedDate())
                .removedBy(job.getRemovedBy())
                .faultDescription(job.getFaultDescription())
                .replacedByProductRefId(job.getReplacedByProductRefId())
                .replacedBySerialNo(job.getReplacedBySerialNo())
                .replacedByCondition(job.getReplacedByCondition())
                .repairMode(job.getRepairMode() == null ? null : job.getRepairMode().name())
                .vendorRefId(job.getVendorRefId())
                .vendorName(job.getVendor() == null ? null : job.getVendor().getSupplierName())
                .vendorDocNo(job.getVendorDocNo())
                .sentDate(job.getSentDate())
                .expectedDate(job.getExpectedDate())
                .receivedDate(job.getReceivedDate())
                .status(job.getStatus() == null ? null : job.getStatus().name())
                .labourCost(zero(job.getLabourCost()))
                .partsCost(zero(job.getPartsCost()))
                .vendorCost(zero(job.getVendorCost()))
                .otherCost(zero(job.getOtherCost()))
                .totalCost(total)
                .resultingUnitCost(job.getResultingUnitCost())
                .newPartCost(job.getNewPartCost())
                .saving(saving)
                .daysInRecon(days)
                .assetStatus(asset == null || asset.getStatus() == null ? null : asset.getStatus().name())
                .assetCondition(asset == null || asset.getCondition() == null
                        ? null : asset.getCondition().name())
                .assetReconCount(asset == null ? null : asset.getReconCount())
                .remarks(job.getRemarks())
                .createdDate(job.getCreatedDate())
                .modifiedDate(job.getModifiedDate())
                .modifiedBy(job.getModifiedBy())
                .costs(costs.stream().map(ReconServiceImpl::toCostDto).collect(Collectors.toList()))
                .build();
    }

    private static ReconCostDto toCostDto(ReconCost line) {
        return ReconCostDto.builder()
                .id(line.getId())
                .reconRefId(line.getReconRefId())
                .costType(line.getCostType() == null ? null : line.getCostType().name())
                .description(line.getDescription())
                .productRefId(line.getProductRefId())
                .productCode(line.getProductMaster() == null ? null : line.getProductMaster().getProdCode())
                .productName(line.getProductMaster() == null ? null : line.getProductMaster().getPname())
                .quantity(line.getQuantity())
                .rate(line.getRate())
                .amount(line.getAmount())
                .supplierRefId(line.getSupplierRefId())
                .supplierName(line.getSupplier() == null ? null : line.getSupplier().getSupplierName())
                .docNo(line.getDocNo())
                .docDate(line.getDocDate())
                .remarks(line.getRemarks())
                .createdBy(line.getCreatedBy())
                .createdDate(line.getCreatedDate())
                .build();
    }

    private static RepairMode parseRepairMode(String raw) {
        try {
            return RepairMode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidRequestException("Repair mode must be one of "
                    + Arrays.toString(RepairMode.values()) + ", got: " + raw);
        }
    }

    private static ReconCostType parseCostType(String raw) {
        try {
            return ReconCostType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidRequestException("Cost type must be one of "
                    + Arrays.toString(ReconCostType.values()) + ", got: " + raw);
        }
    }

    private static ReconStatus parseStatus(String raw) {
        try {
            return ReconStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidRequestException("Status must be one of "
                    + Arrays.toString(ReconStatus.values()) + ", got: " + raw);
        }
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
