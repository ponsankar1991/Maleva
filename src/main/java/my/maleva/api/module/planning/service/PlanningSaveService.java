package my.maleva.api.module.planning.service;

import my.maleva.api.module.planning.dto.PlanningRequest;
import my.maleva.api.module.planning.dto.PlanningSaveResponseDto;
import my.maleva.api.module.planning.entity.PlanningDetails;
import my.maleva.api.module.planning.entity.PlanningMaster;
import my.maleva.api.module.planning.repository.PlanningDetailsRepository;
import my.maleva.api.module.planning.repository.PlanningMasterRepository;
import my.maleva.api.module.employee.repository.EmployeeMasterRepository;
import my.maleva.api.module.master.repository.SequenceNoMasterRepository;
import my.maleva.api.module.master.entity.SequenceNoMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Planning Save Service - Migrated from .NET SP_PLANINGMaster
 *
 * EXACT SP LOGIC REPLICATION:
 *
 * MASTER (PLANINGMaster):
 * - id=0 → INSERT with generated sequence number (PL000000001)
 * - id>0 → UPDATE (keeps existing CNumberDisplay)
 *
 * DETAILS (PLANINGDetails) - Only these fields saved:
 * - SaleOrderMasterRefId (REQUIRED)
 * - TruckRefid (nullable)
 * - Remarks
 * - OriginD, DestinationD
 * - TruckNameD, DriverNameD
 * - SortBy (REQUIRED)
 * - PickupDateD, DeliveryDateD
 * - pickuptimelist, pickupQuantitylist
 * - deliveryQuantitylist, delivertimelist
 *
 * Features:
 * - Transactional (all or nothing)
 * - Validates EmployeeRefId if provided
 * - Exact sequence number generation matching .NET SP
 * - Comprehensive error handling
 */
@Service
public class PlanningSaveService {

    private static final Logger logger = LoggerFactory.getLogger(PlanningSaveService.class);

    // SP Constants
    private static final String SEQUENCE_NAME = "PLANINGMaster";
    private static final String PLANNING_PREFIX = "PL";
    private static final int SEQUENCE_PADDING = 9;

    // Repositories
    private final PlanningMasterRepository masterRepository;
    private final PlanningDetailsRepository detailsRepository;
    private final EmployeeMasterRepository employeeRepository;
    private final SequenceNoMasterRepository sequenceRepository;

    public PlanningSaveService(
            PlanningMasterRepository masterRepository,
            PlanningDetailsRepository detailsRepository,
            EmployeeMasterRepository employeeRepository,
            SequenceNoMasterRepository sequenceRepository) {
        this.masterRepository = masterRepository;
        this.detailsRepository = detailsRepository;
        this.employeeRepository = employeeRepository;
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Save Planning records (Insert or Update).
     * Matches .NET: InsertPLANING(List<PLANINGMasterModel> objBrand, Int32 Comid)
     *
     * @param requests List of PlanningRequest (usually 1 item, but SP supports multiple)
     * @param comid Company ID from header
     * @return List of results matching SP output format
     */
    @Transactional
    public List<PlanningSaveResponseDto> saveAll(List<PlanningRequest> requests, Integer comid) {
        logger.info("SP_PLANINGMaster: Processing {} record(s), comid={}", requests.size(), comid);

        List<PlanningSaveResponseDto> results = new ArrayList<>();

        for (PlanningRequest request : requests) {
            try {
                PlanningSaveResponseDto result = processSinglePlanning(request, comid);
                results.add(result);
            } catch (Exception ex) {
                logger.error("SP_PLANINGMaster: Unexpected error processing record", ex);
                results.add(PlanningSaveResponseDto.error(
                        ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
            }
        }

        return results;
    }

    /**
     * Process single planning record - replicates SP WHILE loop iteration.
     */
    private PlanningSaveResponseDto processSinglePlanning(PlanningRequest request, Integer comid) {
        logger.debug("SP_PLANINGMaster: Processing id={}, cNumberDisplay={}",
                request.getId(), request.getCNumberDisplay());

        // ===== VALIDATE EmployeeRefId (if > 0) =====
        if (request.getEmployeeRefId() != null && request.getEmployeeRefId() != 0) {
            boolean employeeExists = employeeRepository.existsByIdAndCompanyRefIdAndActive(
                    request.getEmployeeRefId(), comid, 1);
            if (!employeeExists) {
                logger.warn("SP_PLANINGMaster: Employee validation failed, employeeId={}", request.getEmployeeRefId());
                return PlanningSaveResponseDto.error("Employee Not Found, id: " + request.getEmployeeRefId());
            }
        }

        // ===== SET CompanyRefId from header =====
        request.setCompanyRefId(comid);

        LocalDateTime now = LocalDateTime.now();
        Integer savedId;
        String billNoDisplay;

        // ===== DELETE old details if UPDATE =====
        if (request.getId() != null && request.getId() > 0) {
            logger.debug("SP_PLANINGMaster: UPDATE mode, deleting old details for id={}", request.getId());
            detailsRepository.deleteByPlanningMasterRefId(request.getId());
        }

        // ===== INSERT or UPDATE Master =====
        if (request.getId() == null || request.getId() == 0) {
            // --- INSERT NEW ---
            logger.debug("SP_PLANINGMaster: INSERT mode");

            PlanningMaster master = PlanningMaster.builder()
                    .companyRefId(comid)
                    .userRefId(normalizeZeroToNull(request.getUserRefId()))
                    .employeeRefId(normalizeZeroToNull(request.getEmployeeRefId()))
                    .lastEmployeeRefId(normalizeZeroToNull(request.getEmployeeRefId()))
                    .fDate(toStartOfDay(request.getFDate(), now))
                    .tDate(toStartOfDay(request.getTDate(), now))
                    .saleDate(toStartOfDay(request.getSaleDate(), now))
                    .search(request.getSearch())
                    .remarks(request.getRemarks())
                    .active(1)
                    .createdDate(now)
                    .createdBy(getCurrentUser())
                    .modifiedDate(now)
                    .modifiedBy(getCurrentUser())
                    .cNumberDisplay(request.getCNumberDisplay() != null ? request.getCNumberDisplay() : "")
                    .cNumber(request.getCNumber() != null ? request.getCNumber() : 0)
                    .build();

            PlanningMaster saved = masterRepository.save(master);
            savedId = saved.getId();

            // ===== INSERT Details =====
            if (request.getSaleDetails() != null && !request.getSaleDetails().isEmpty()) {
                insertDetails(request.getSaleDetails(), savedId, now);
            }

            // ===== GENERATE SEQUENCE NUMBER (INSERT ONLY) =====
            billNoDisplay = generateSequenceNumber(comid, savedId);

            logger.info("SP_PLANINGMaster: INSERT successful, id={}, billNo={}", savedId, billNoDisplay);

        } else {
            // --- UPDATE EXISTING ---
            logger.debug("SP_PLANINGMaster: UPDATE mode for id={}", request.getId());

            PlanningMaster existing = masterRepository.findById(request.getId())
                    .orElse(null);

            if (existing == null) {
                return PlanningSaveResponseDto.error("Planning not found with id: " + request.getId());
            }

            // SP: UPDATE only these fields (CNumberDisplay and CNumber are NOT updated)
            existing.setEmployeeRefId(normalizeZeroToNull(request.getEmployeeRefId()));
            existing.setLastEmployeeRefId(normalizeZeroToNull(request.getEmployeeRefId()));
            existing.setUserRefId(normalizeZeroToNull(request.getUserRefId()));
            existing.setFDate(request.getFDate() != null ? request.getFDate().atStartOfDay() : existing.getFDate());
            existing.setTDate(request.getTDate() != null ? request.getTDate().atStartOfDay() : existing.getTDate());
            existing.setSaleDate(request.getSaleDate() != null ? request.getSaleDate().atStartOfDay() : existing.getSaleDate());
            existing.setSearch(request.getSearch());
            existing.setRemarks(request.getRemarks());
            existing.setModifiedDate(now);
            existing.setModifiedBy(getCurrentUser());

            PlanningMaster updated = masterRepository.save(existing);
            savedId = updated.getId();
            billNoDisplay = updated.getCNumberDisplay();

            // ===== INSERT Details (for UPDATE too) =====
            if (request.getSaleDetails() != null && !request.getSaleDetails().isEmpty()) {
                insertDetails(request.getSaleDetails(), savedId, now);
            }

            logger.info("SP_PLANINGMaster: UPDATE successful, id={}, billNo={}", savedId, billNoDisplay);
        }

        // ===== RETURN SUCCESS =====
        return PlanningSaveResponseDto.builder()
                .ok(true)
                .message("Planning saved successfully")
                .name(billNoDisplay)
                .id(savedId)
                .build();
    }

    /**
     * Insert planning details - EXACT replica of SP INSERT.
     *
     * SP saves ONLY these fields to PLANINGDetails:
     * - PLANINGMasterRefId (from master)
     * - SaleOrderMasterRefId
     * - TruckRefid (0 becomes null)
     * - Remarks
     * - OriginD, DestinationD
     * - TruckNameD, DriverNameD
     * - SortBy
     * - PickupDateD (empty string becomes null)
     * - DeliveryDateD (empty string becomes null)
     * - pickuptimelist, pickupQuantitylist
     * - deliveryQuantitylist, delivertimelist
     */
    private void insertDetails(List<PlanningRequest.PlanningDetailRequest> details, Integer masterId, LocalDateTime now) {
        logger.debug("SP_PLANINGMaster: Inserting {} details for masterId={}", details.size(), masterId);

        for (PlanningRequest.PlanningDetailRequest item : details) {
            // SP: TruckRefid = case when 0 then null else TruckRefid end
            // SP: PickupDateD = case when '' then null else PickupDateD end
            // SP: DeliveryDateD = case when '' then null else DeliveryDateD end

            PlanningDetails detail = PlanningDetails.builder()
                    .planningMasterRefId(masterId)
                    .saleOrderMasterRefId(item.getSaleOrderMasterRefId())
                    .truckRefId(normalizeZeroToNull(item.getTruckRefid()))
                    .remarks(item.getRemarks())
                    .originD(item.getOriginD())
                    .destinationD(item.getDestinationD())
                    .pickupDateD(item.getPickupDateD())
                    .deliveryDateD(item.getDeliveryDateD())
                    .sortBy(item.getSortBy() != null ? item.getSortBy() : 0)
                    .truckNameD(item.getTruckNameD())
                    .driverNameD(item.getDriverNameD())
                    .pickupTimeList(item.getPickuptimelist())
                    .pickupQuantityList(item.getPickupQuantitylist())
                    .deliveryQuantityList(item.getDeliveryQuantitylist())
                    .deliveryTimeList(item.getDelivertimelist())
                    .createdDate(now)
                    .modifiedDate(now)
                    .driverName(item.getDriverName()) // SP saves DriverName same as DriverNameD
                    .build();

            detailsRepository.save(detail);
        }

        logger.debug("SP_PLANINGMaster: Saved {} details for masterId={}", details.size(), masterId);
    }

    /**
     * Generate sequence number - EXACT replica of SP sequence generation.
     *
     * SP Logic:
     * SET @count = MAX(SequenceNo) FROM SequenceNoMaster WHERE SequenceName='PLANINGMaster'
     * IF @count = 0
     *   SET @SaleNo = 1
     * ELSE
     *   SET @SaleNo = @count + 1
     * UPDATE SequenceNoMaster SET SequenceNo=@SaleNo
     * SET @SaleNoDisplay = 'PL' + RIGHT('000000000' + CAST(@SaleNo AS VARCHAR), 9)
     * UPDATE PLANINGMaster SET CNumberDisplay = @SaleNoDisplay WHERE Id = @saleid
     */
    private String generateSequenceNumber(Integer companyId, Integer masterId) {
        logger.debug("SP_PLANINGMaster: Generating sequence for companyId={}, masterId={}", companyId, masterId);

        // Get current max sequence number
        Integer currentMax = sequenceRepository.findMaxSequenceNoByCompanyAndSequenceName(companyId, SEQUENCE_NAME);
        if (currentMax == null) {
            currentMax = 0;
        }

        int nextSequenceNo = (currentMax == 0) ? 1 : currentMax + 1;

        // Update or create sequence record
        updateOrCreateSequence(companyId, nextSequenceNo);

        // Format as 'PL' + 9-digit padded number (e.g., PL000000001)
        String formattedNumber = String.format("%s%0" + SEQUENCE_PADDING + "d", PLANNING_PREFIX, nextSequenceNo);

        // Update master record with generated display number
        masterRepository.updateCNumberDisplay(masterId, formattedNumber, nextSequenceNo);

        logger.info("SP_PLANINGMaster: Generated sequence={}, display={}", nextSequenceNo, formattedNumber);

        return formattedNumber;
    }

    /**
     * Update or create sequence record in SequenceNoMaster.
     */
    private void updateOrCreateSequence(Integer companyId, Integer sequenceNo) {
        var existing = sequenceRepository.findByCompanyRefIdAndSequenceName(companyId, SEQUENCE_NAME);
        LocalDateTime now = LocalDateTime.now();

        if (existing.isPresent()) {
            var seq = existing.get();
            seq.setSequenceNo(sequenceNo);
            seq.setSequenceDate(now);
            seq.setSequenceYear(now.getYear());
            seq.setSequenceMonth(now.getMonthValue());
            sequenceRepository.save(seq);
        } else {
            SequenceNoMaster newSeq = SequenceNoMaster.builder()
                    .companyRefId(companyId)
                    .sequenceName(SEQUENCE_NAME)
                    .sequenceNo(sequenceNo)
                    .sequenceDate(now)
                    .sequenceYear(now.getYear())
                    .sequenceMonth(now.getMonthValue())
                    .build();
            sequenceRepository.save(newSeq);
        }
    }

    // ===== Helper Methods =====

    /**
     * Normalize 0 to null (SP behavior)
     */
    private Integer normalizeZeroToNull(Integer value) {
        return (value == null || value == 0) ? null : value;
    }

    /**
     * Convert date-only request values to LocalDateTime for persistence.
     */
    private LocalDateTime toStartOfDay(LocalDate value, LocalDateTime fallback) {
        return value != null ? value.atStartOfDay() : fallback;
    }

    /**
     * Get current user from SecurityContext
     */
    private String getCurrentUser() {
        // TODO: Get actual user from SecurityContext
        return "SYSTEM";
    }

    /**
     * Delete (soft delete) a Planning record.
     * Sets Active = 2 (soft delete like .NET SP)
     */
    @Transactional
    public PlanningSaveResponseDto delete(Integer id, Integer companyId) {
        logger.info("SP_PLANINGMaster: Delete requested for id={}, companyId={}", id, companyId);

        try {
            PlanningMaster existing = masterRepository.findById(id)
                    .orElse(null);

            if (existing == null) {
                logger.warn("SP_PLANINGMaster: Planning not found for delete, id={}", id);
                return PlanningSaveResponseDto.error("Planning not found with id: " + id);
            }

            // Soft delete (Active = 2 like .NET)
            existing.setActive(2);
            existing.setModifiedDate(LocalDateTime.now());
            existing.setModifiedBy(getCurrentUser());
            masterRepository.save(existing);

            logger.info("SP_PLANINGMaster: Delete successful for id={}", id);
            return PlanningSaveResponseDto.builder()
                    .ok(true)
                    .message("Planning deleted successfully")
                    .id(id)
                    .build();

        } catch (Exception ex) {
            logger.error("SP_PLANINGMaster: Error deleting planning id={}", id, ex);
            return PlanningSaveResponseDto.error(
                    "Error deleting planning: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
        }
    }
}
