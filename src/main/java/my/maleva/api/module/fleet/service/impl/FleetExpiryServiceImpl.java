package my.maleva.api.module.fleet.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.dto.ExpiryAlertDto;
import my.maleva.api.module.fleet.dto.MaintenanceDashboardDto;
import my.maleva.api.module.fleet.dto.OpenJobOrderDto;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.DriverMasterRepository;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.joborder.entity.JobOrderMaster;
import my.maleva.api.module.joborder.repository.JobOrderMasterRepository;
import my.maleva.api.module.fleet.service.FleetExpiryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Flattens the expiry columns of every truck and driver into one ranked list.
 *
 * The dates live as columns, not rows - fourteen on TruckMaster, twelve on
 * DriverMaster - so a straight query cannot sort the fleet by urgency. Each
 * column is declared once below and the same rule then applies to all of them.
 */
@Service
public class FleetExpiryServiceImpl implements FleetExpiryService {

    private static final Integer ACTIVE = 1;

    /**
     * MalevaTruck = 1 marks a truck as Maleva-owned rather than a
     * subcontractor's vehicle sharing the same TruckMaster table. Only these
     * are alerted on here: a subcontractor's paperwork is not ours to renew.
     */
    private static final Integer MALEVA_OWNED = 1;

    private static final int DEFAULT_HORIZON_DAYS = 10;
    private static final int DEFAULT_CRITICAL_DAYS = 5;

    private static final String ENTITY_TRUCK = "TRUCK";
    private static final String ENTITY_DRIVER = "DRIVER";

    private static final String GROUP_DOCUMENT = "DOCUMENT";
    private static final String GROUP_SERVICE = "SERVICE";
    private static final String GROUP_PORT_PASS = "PORT_PASS";

    private static final String SEVERITY_EXPIRED = "EXPIRED";
    private static final String SEVERITY_CRITICAL = "CRITICAL";
    private static final String SEVERITY_WARNING = "WARNING";

    /** One expiry column: what it is called on screen, how to read it, and its group. */
    private record ExpiryColumn<T>(String category, String group, Function<T, LocalDate> reader) { }

    /**
     * The truck columns worth alerting on.
     *
     * RotexMyExp1, RotexSGExp1 and PuspacomExp1 are left out: they are the
     * second-vehicle variants and are not maintained on this data.
     */
    private static final List<ExpiryColumn<TruckMaster>> TRUCK_COLUMNS = List.of(
            new ExpiryColumn<>("Insurance", GROUP_DOCUMENT, TruckMaster::getInsuranceExp),
            new ExpiryColumn<>("Rotex MY", GROUP_DOCUMENT, TruckMaster::getRotexMyExp),
            new ExpiryColumn<>("Rotex SG", GROUP_DOCUMENT, TruckMaster::getRotexSGExp),
            new ExpiryColumn<>("Puspakom", GROUP_DOCUMENT, TruckMaster::getPuspacomExp),
            new ExpiryColumn<>("Bonam", GROUP_DOCUMENT, TruckMaster::getBonamExp),
            new ExpiryColumn<>("APAD", GROUP_DOCUMENT, TruckMaster::getApadExp),
            new ExpiryColumn<>("PTP Sticker", GROUP_DOCUMENT, TruckMaster::getPtpStickerExp),
            new ExpiryColumn<>("Service", GROUP_SERVICE, TruckMaster::getServiceExp),
            new ExpiryColumn<>("Alignment", GROUP_SERVICE, TruckMaster::getAlignmentExp),
            new ExpiryColumn<>("Grease", GROUP_SERVICE, TruckMaster::getGreeceExp),
            new ExpiryColumn<>("Gear Oil", GROUP_SERVICE, TruckMaster::getGearOilExp));

    /** The driver columns. The port dates are pass expiries. */
    private static final List<ExpiryColumn<DriverMaster>> DRIVER_COLUMNS = List.of(
            new ExpiryColumn<>("Licence", GROUP_DOCUMENT, DriverMaster::getLicenseExp),
            new ExpiryColumn<>("GDL", GROUP_DOCUMENT, DriverMaster::getGdlExp),
            new ExpiryColumn<>("PTP Pass", GROUP_PORT_PASS, DriverMaster::getPtpPort),
            new ExpiryColumn<>("Westport Pass", GROUP_PORT_PASS, DriverMaster::getWestportPort),
            new ExpiryColumn<>("Northport Pass", GROUP_PORT_PASS, DriverMaster::getNorthportPort),
            new ExpiryColumn<>("PKFZ Pass", GROUP_PORT_PASS, DriverMaster::getPkfzPort),
            new ExpiryColumn<>("KLIA Pass", GROUP_PORT_PASS, DriverMaster::getKliaPort),
            new ExpiryColumn<>("Kuantan Pass", GROUP_PORT_PASS, DriverMaster::getKuantanPort),
            new ExpiryColumn<>("PGU Pass", GROUP_PORT_PASS, DriverMaster::getPguPort),
            new ExpiryColumn<>("Tanjung Pass", GROUP_PORT_PASS, DriverMaster::getTanjungPort),
            new ExpiryColumn<>("Penang Pass", GROUP_PORT_PASS, DriverMaster::getPenangPort));

    /**
     * Dates at or before this are placeholders, not real expiries.
     *
     * Several rows carry 1900-01-01 where the field was never filled in, and
     * treating those as "expired 46,000 days ago" would drown the screen.
     */
    private static final LocalDate PLACEHOLDER_CUTOFF = LocalDate.of(1901, 1, 1);

    /**
     * JobOrderStatusMaster ids that mean the job is finished with.
     * 3 = Completed, 4 = Cancelled. Everything else counts as still open.
     */
    private static final List<Integer> CLOSED_JOB_STATUS_IDS = List.of(3, 4);

    private final TruckMasterRepository truckMasterRepository;
    private final DriverMasterRepository driverMasterRepository;
    private final JobOrderMasterRepository jobOrderMasterRepository;

    public FleetExpiryServiceImpl(TruckMasterRepository truckMasterRepository,
                                  DriverMasterRepository driverMasterRepository,
                                  JobOrderMasterRepository jobOrderMasterRepository) {
        this.truckMasterRepository = truckMasterRepository;
        this.driverMasterRepository = driverMasterRepository;
        this.jobOrderMasterRepository = jobOrderMasterRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceDashboardDto getDashboard(Integer companyRefId,
                                                Integer horizonDays,
                                                Integer criticalDays) {
        if (companyRefId == null || companyRefId == 0) {
            throw new InvalidRequestException("companyRefId is required");
        }

        int horizon = horizonDays == null || horizonDays <= 0 ? DEFAULT_HORIZON_DAYS : horizonDays;
        int critical = criticalDays == null || criticalDays <= 0 ? DEFAULT_CRITICAL_DAYS : criticalDays;
        if (critical > horizon) {
            throw new InvalidRequestException("criticalDays must not exceed horizonDays");
        }

        LocalDate today = LocalDate.now();
        LocalDate horizonDate = today.plusDays(horizon);

        List<TruckMaster> trucks = truckMasterRepository
                .findByCompanyRefIdAndActiveAndMalevaTruck(companyRefId, ACTIVE, MALEVA_OWNED);
        List<DriverMaster> drivers = driverMasterRepository.findByCompanyRefIdAndActive(companyRefId, ACTIVE);

        List<ExpiryAlertDto> alerts = new ArrayList<>();

        for (TruckMaster truck : trucks) {
            for (ExpiryColumn<TruckMaster> column : TRUCK_COLUMNS) {
                ExpiryAlertDto alert = toAlert(
                        ENTITY_TRUCK, truck.getId(), truck.getTruckName(), truck.getTruckNumber(),
                        column, column.reader().apply(truck), today, horizonDate, critical);
                if (alert != null) {
                    alerts.add(alert);
                }
            }
        }

        for (DriverMaster driver : drivers) {
            for (ExpiryColumn<DriverMaster> column : DRIVER_COLUMNS) {
                ExpiryAlertDto alert = toAlert(
                        ENTITY_DRIVER, driver.getId(), driver.getDriverName(), driver.getLicenseNo(),
                        column, column.reader().apply(driver), today, horizonDate, critical);
                if (alert != null) {
                    alerts.add(alert);
                }
            }
        }

        // Most urgent first: the longest overdue at the top, then by how soon.
        alerts.sort(Comparator
                .comparing(ExpiryAlertDto::getDaysRemaining)
                .thenComparing(ExpiryAlertDto::getEntityName, Comparator.nullsLast(String::compareTo))
                .thenComparing(ExpiryAlertDto::getCategory));

        List<OpenJobOrderDto> openJobs = openJobs(companyRefId, today);

        return MaintenanceDashboardDto.builder()
                .asOf(today)
                .horizonDays(horizon)
                .criticalDays(critical)
                .activeTrucks(trucks.size())
                .activeDrivers(drivers.size())
                .expiredCount(countBySeverity(alerts, SEVERITY_EXPIRED))
                .criticalCount(countBySeverity(alerts, SEVERITY_CRITICAL))
                .warningCount(countBySeverity(alerts, SEVERITY_WARNING))
                .trucksNeedingAttention(distinctEntities(alerts, ENTITY_TRUCK))
                .driversNeedingAttention(distinctEntities(alerts, ENTITY_DRIVER))
                .byCategory(countByCategory(alerts))
                .alerts(alerts)
                .openJobCount(openJobs.size())
                .overdueJobCount((int) openJobs.stream().filter(OpenJobOrderDto::isOverdue).count())
                .openJobs(openJobs)
                .build();
    }

    /**
     * Turns one date into an alert, or null when it does not need reporting.
     *
     * Null and placeholder dates are dropped, and so is anything beyond the
     * horizon - a service due in three weeks is not yet a problem and would only
     * bury the things that are.
     */
    private <T> ExpiryAlertDto toAlert(String entityType,
                                       Integer entityId,
                                       String entityName,
                                       String entityRef,
                                       ExpiryColumn<T> column,
                                       LocalDate expiry,
                                       LocalDate today,
                                       LocalDate horizonDate,
                                       int criticalDays) {
        if (expiry == null || expiry.isBefore(PLACEHOLDER_CUTOFF) || expiry.isAfter(horizonDate)) {
            return null;
        }

        long days = ChronoUnit.DAYS.between(today, expiry);
        String severity = days < 0 ? SEVERITY_EXPIRED
                : days <= criticalDays ? SEVERITY_CRITICAL
                : SEVERITY_WARNING;

        return ExpiryAlertDto.builder()
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .entityRef(entityRef)
                .category(column.category())
                .group(column.group())
                .expiryDate(expiry)
                .daysRemaining((int) days)
                .severity(severity)
                .build();
    }

    /**
     * Workshop jobs still open, most overdue first.
     *
     * Open means not Completed and not Cancelled, so `assign` and `InProgress`
     * both qualify. Filtering on InProgress alone would return nothing on the
     * current data - all six open jobs sit at `assign`, because InProgress is
     * flagged inactive in JobOrderStatusMaster and cannot be selected.
     */
    private List<OpenJobOrderDto> openJobs(Integer companyRefId, LocalDate today) {
        return jobOrderMasterRepository.findOpenJobs(companyRefId, CLOSED_JOB_STATUS_IDS).stream()
                .map(job -> toOpenJob(job, today))
                .sorted(Comparator.comparing(
                        OpenJobOrderDto::getDaysRemaining,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private OpenJobOrderDto toOpenJob(JobOrderMaster job, LocalDate today) {
        LocalDate expected = job.getExpectedCompletionDate();
        Integer daysRemaining = expected == null
                ? null
                : (int) ChronoUnit.DAYS.between(today, expected);

        return OpenJobOrderDto.builder()
                .id(job.getId())
                .jobNo(job.getCNumberDisplay())
                .truckRefId(job.getTruck() == null ? null : job.getTruck().getId())
                .truckName(job.getTruck() == null ? null : job.getTruck().getTruckName())
                .statusRefId(job.getStatus() == null ? null : job.getStatus().getId())
                .statusName(job.getStatus() == null ? null : job.getStatus().getStatusName())
                .priorityName(job.getPriority() == null ? null : job.getPriority().getPriorityName())
                .vendorName(job.getVendorName())
                .problemName(job.getProblemName())
                .jobDate(job.getJobDate())
                .expectedCompletionDate(expected)
                .daysRemaining(daysRemaining)
                .overdue(daysRemaining != null && daysRemaining < 0)
                .estimatedCost(job.getEstimatedCost())
                .build();
    }

    private int countBySeverity(List<ExpiryAlertDto> alerts, String severity) {
        return (int) alerts.stream().filter(a -> severity.equals(a.getSeverity())).count();
    }

    private int distinctEntities(List<ExpiryAlertDto> alerts, String entityType) {
        return (int) alerts.stream()
                .filter(a -> entityType.equals(a.getEntityType()))
                .map(ExpiryAlertDto::getEntityId)
                .distinct()
                .count();
    }

    /** Ordered by how many alerts each category has, so the worst reads first. */
    private Map<String, Integer> countByCategory(List<ExpiryAlertDto> alerts) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        alerts.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ExpiryAlertDto::getCategory, java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> counts.put(entry.getKey(), entry.getValue().intValue()));
        return counts;
    }
}
