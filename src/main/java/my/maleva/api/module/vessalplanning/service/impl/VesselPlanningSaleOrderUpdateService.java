package my.maleva.api.module.vessalplanning.service.impl;

import my.maleva.api.common.exception.EntityNotFoundException;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.boardingsettlement.service.BoardingEventSyncService;
import my.maleva.api.module.jobs.entity.JobStatusMaster;
import my.maleva.api.module.jobs.repository.JobStatusMasterRepository;
import my.maleva.api.module.saleorder.entity.SaleOrderMaster;
import my.maleva.api.module.saleorder.repository.SaleOrderMasterRepository;
import my.maleva.api.module.vessalplanning.dto.VesselPlanningSaleOrderUpdateRequest;
import my.maleva.api.module.vessalplanning.dto.VesselPlanningSaleOrderUpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * The Vessel Planning screen's Update window.
 *
 * Writes the fields that window shows - job status, cargo, PTW, the loading and off vessel
 * ETA/ETB/ETD, and the three loading and three off vessel boarding officers with their amounts.
 * Nothing else on the sale order is touched. It replaced a full PUT of the sale order built from
 * a form that never loaded the totals, which set GrossAmount, TaxAmount and Amount to 0.
 */
@Service
public class VesselPlanningSaleOrderUpdateService {

    private static final Logger logger = LoggerFactory.getLogger(VesselPlanningSaleOrderUpdateService.class);

    private static final int DELETED_STATUS = 2;
    private static final int INACTIVE_STATUS = 0;

    private static final DateTimeFormatter GRID_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<DateTimeFormatter> ACCEPTED_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    );

    private final SaleOrderMasterRepository saleOrderRepository;
    private final JobStatusMasterRepository jobStatusRepository;
    private final BoardingEventSyncService boardingEventSyncService;

    public VesselPlanningSaleOrderUpdateService(SaleOrderMasterRepository saleOrderRepository,
                                                JobStatusMasterRepository jobStatusRepository,
                                                BoardingEventSyncService boardingEventSyncService) {
        this.saleOrderRepository = saleOrderRepository;
        this.jobStatusRepository = jobStatusRepository;
        this.boardingEventSyncService = boardingEventSyncService;
    }

    @Transactional
    public VesselPlanningSaleOrderUpdateResponse update(VesselPlanningSaleOrderUpdateRequest request) {
        Integer saleOrderId = request.getSaleOrderId();
        Integer companyId = request.getCompanyId();

        SaleOrderMaster job = findJob(saleOrderId, companyId);

        // Everything is parsed and checked before the write, so a bad value leaves the job as it was.
        LocalDateTime eta = resolveDate(request.getEta(), job.getEta(), "L ETA");
        LocalDateTime etb = resolveDate(request.getEtb(), job.getEtb(), "L ETB");
        LocalDateTime etd = resolveDate(request.getEtd(), job.getEtd(), "L ETD");
        LocalDateTime oeta = resolveDate(request.getOeta(), job.getOeta(), "O ETA");
        LocalDateTime oetb = resolveDate(request.getOetb(), job.getOetb(), "O ETB");
        LocalDateTime oetd = resolveDate(request.getOetd(), job.getOetd(), "O ETD");

        Integer statusId = resolveStatus(request.getJobStatusId(), job, companyId);
        String cargo = isBlank(request.getCargo()) ? job.getCargo() : request.getCargo().trim();
        String ptw = request.getPtw() == null ? job.getPtw() : request.getPtw().trim();

        Officers loading = resolveOfficers(
                request.getLoadingOfficer1(), request.getLoadingOfficer2(), request.getLoadingOfficer3(),
                new Officers(job.getLBoardingOfficerRefid(), job.getLBoardingOfficer1Refid(), job.getLBoardingOfficer2Refid(),
                        job.getLBoardingAmount(), job.getLBoardingAmount1(), job.getLBoardingAmount2()));
        Officers off = resolveOfficers(
                request.getOffOfficer1(), request.getOffOfficer2(), request.getOffOfficer3(),
                new Officers(job.getOBoardingOfficerRefid(), job.getOBoardingOfficer1Refid(), job.getOBoardingOfficer2Refid(),
                        job.getOBoardingAmount(), job.getOBoardingAmount1(), job.getOBoardingAmount2()));

        saleOrderRepository.updateVesselPlanningFields(saleOrderId, companyId,
                statusId, cargo, ptw,
                eta, etb, etd, oeta, oetb, oetd,
                loading.officer1(), loading.officer2(), loading.officer3(),
                loading.amount1(), loading.amount2(), loading.amount3(),
                off.officer1(), off.officer2(), off.officer3(),
                off.amount1(), off.amount2(), off.amount3(),
                LocalDateTime.now());

        // The boarding settlement events are built from the job's officers and dates - the same
        // sync the Sale Order screen's save runs. The update cleared the persistence context, so
        // this reload reads what was just written.
        SaleOrderMaster saved = findJob(saleOrderId, companyId);
        boardingEventSyncService.syncEventsForJob(saved);

        logger.info("Vessel planning update saved - saleOrderId: {}, company: {}, status: {}, loading officers: {}/{}/{}, off officers: {}/{}/{}",
                saleOrderId, companyId, statusId,
                loading.officer1(), loading.officer2(), loading.officer3(),
                off.officer1(), off.officer2(), off.officer3());

        return VesselPlanningSaleOrderUpdateResponse.builder()
                .ok(true)
                .message("Sale order updated successfully")
                .saleOrderId(saleOrderId)
                .jobStatusId(saved.getJStatus())
                .jobStatus(statusName(saved.getJStatus()))
                .cargo(emptyIfNull(saved.getCargo()))
                .ptw(emptyIfNull(saved.getPtw()))
                .seta(formatForGrid(saved.getEta()))
                .setb(formatForGrid(saved.getEtb()))
                .setd(formatForGrid(saved.getEtd()))
                .soeta(formatForGrid(saved.getOeta()))
                .soetb(formatForGrid(saved.getOetb()))
                .soetd(formatForGrid(saved.getOetd()))
                .loadingOfficer1(saved.getLBoardingOfficerRefid())
                .loadingOfficer2(saved.getLBoardingOfficer1Refid())
                .loadingOfficer3(saved.getLBoardingOfficer2Refid())
                .loadingAmount1(saved.getLBoardingAmount())
                .loadingAmount2(saved.getLBoardingAmount1())
                .loadingAmount3(parseAmount(saved.getLBoardingAmount2()))
                .offOfficer1(saved.getOBoardingOfficerRefid())
                .offOfficer2(saved.getOBoardingOfficer1Refid())
                .offOfficer3(saved.getOBoardingOfficer2Refid())
                .offAmount1(saved.getOBoardingAmount())
                .offAmount2(saved.getOBoardingAmount1())
                .offAmount3(parseAmount(saved.getOBoardingAmount2()))
                .build();
    }

    private SaleOrderMaster findJob(Integer saleOrderId, Integer companyId) {
        return saleOrderRepository.findByIdAndCompanyRefId(saleOrderId, companyId)
                .filter(order -> !Objects.equals(order.getActive(), DELETED_STATUS))
                .orElseThrow(() -> new EntityNotFoundException("Sale order not found: " + saleOrderId));
    }

    /**
     * Same rules as the Sale Order screen's status change: the status must be this company's and
     * active, and a job with purchase orders still open cannot move to WAITING FOR BILLING or
     * WAITING FOR POD.
     */
    private Integer resolveStatus(Integer requestedId, SaleOrderMaster job, Integer companyId) {
        if (requestedId == null || requestedId <= 0 || requestedId.equals(job.getJStatus())) {
            return job.getJStatus();
        }

        JobStatusMaster status = jobStatusRepository.findById(requestedId)
                .filter(s -> Objects.equals(s.getCompanyRefId(), companyId))
                .filter(s -> !Objects.equals(s.getActive(), INACTIVE_STATUS))
                .orElseThrow(() -> new InvalidRequestException("Invalid Job Status ID"));

        String name = status.getName() == null ? "" : status.getName().trim().toUpperCase(Locale.ROOT);
        boolean closesJob = status.getId() == 15 || "WAITING FOR BILLING".equals(name)
                || status.getId() == 20 || "WAITING FOR POD".equals(name);
        if (closesJob && saleOrderRepository.countPendingPortCharges(companyId, job.getId()) > 0) {
            throw new InvalidRequestException("Please complete all purchase orders before changing status!");
        }
        return status.getId();
    }

    /**
     * The officers as the window shows them. Amounts follow the Sale Order screen's rule
     * (1 officer 50, 2 officers 30 each, 3 officers 20 each), but only when that side's officers
     * actually changed - an untouched side keeps the amounts already stored on the job.
     */
    static Officers resolveOfficers(Integer requested1, Integer requested2, Integer requested3, Officers stored) {
        Integer officer1 = positiveOrNull(requested1);
        Integer officer2 = positiveOrNull(requested2);
        Integer officer3 = positiveOrNull(requested3);

        boolean unchanged = Objects.equals(officer1, positiveOrNull(stored.officer1()))
                && Objects.equals(officer2, positiveOrNull(stored.officer2()))
                && Objects.equals(officer3, positiveOrNull(stored.officer3()));
        if (unchanged) {
            return stored;
        }

        int count = (officer1 != null ? 1 : 0) + (officer2 != null ? 1 : 0) + (officer3 != null ? 1 : 0);
        double each = switch (count) {
            case 1 -> 50;
            case 2 -> 30;
            case 3 -> 20;
            default -> 0;
        };
        return new Officers(officer1, officer2, officer3,
                officer1 != null ? each : 0.0,
                officer2 != null ? each : 0.0,
                formatAmount(officer3 != null ? each : 0.0));
    }

    /** Null keeps the stored date, blank clears it, anything else must parse. */
    static LocalDateTime resolveDate(String value, LocalDateTime stored, String label) {
        if (value == null) {
            return stored;
        }
        if (value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        for (DateTimeFormatter format : ACCEPTED_FORMATS) {
            try {
                return LocalDateTime.parse(trimmed, format);
            } catch (DateTimeParseException ignored) {
                // Try the next accepted format.
            }
        }
        throw new InvalidRequestException(label + " is not a valid date/time: " + trimmed);
    }

    private String statusName(Integer statusId) {
        if (statusId == null || statusId <= 0) {
            return "";
        }
        return jobStatusRepository.findById(statusId)
                .map(status -> emptyIfNull(status.getName()).trim())
                .orElse("");
    }

    /** LBoardingAmount2 / OBoardingAmount2 are varchar columns, unlike the first two amounts. */
    private static String formatAmount(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static Double parseAmount(String value) {
        if (isBlank(value)) {
            return 0.0;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private static Integer positiveOrNull(Integer value) {
        return value != null && value > 0 ? value : null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private static String formatForGrid(LocalDateTime value) {
        return value == null ? "" : value.format(GRID_FORMAT);
    }

    record Officers(Integer officer1, Integer officer2, Integer officer3,
                    Double amount1, Double amount2, String amount3) {
    }
}
