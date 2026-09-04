package my.maleva.api.module.gps.service.impl;

import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.gps.client.WialonClient;
import my.maleva.api.module.gps.client.WialonRowMapper;
import my.maleva.api.module.gps.config.WialonProperties;
import my.maleva.api.module.gps.dto.GpsEngineHourRow;
import my.maleva.api.module.gps.dto.GpsFillingRow;
import my.maleva.api.module.gps.dto.GpsReportSyncResult;
import my.maleva.api.module.gps.dto.GpsSyncResultDto;
import my.maleva.api.module.gps.dto.wialon.WialonReportExecResponse;
import my.maleva.api.module.gps.dto.wialon.WialonReportTable;
import my.maleva.api.module.gps.dto.wialon.WialonReportTemplate;
import my.maleva.api.module.gps.dto.wialon.WialonResourceItem;
import my.maleva.api.module.gps.dto.wialon.WialonResultRow;
import my.maleva.api.module.gps.dto.wialon.WialonSession;
import my.maleva.api.module.gps.dto.wialon.WialonUnitItem;
import my.maleva.api.module.gps.exception.WialonApiException;
import my.maleva.api.module.gps.service.GpsPersistenceService;
import my.maleva.api.module.gps.service.GpsSyncService;
import my.maleva.api.module.gps.service.TruckResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Orchestrates the Wialon pull.
 *
 * Faithful to the legacy GPSJob in what it fetches and writes; different in how
 * it fails. The legacy job chained its steps inside each other's success branch
 * and swallowed every error, so a single bad report stopped the rest of the run
 * without a trace. Here the three reports are independent and each one records
 * its own status.
 */
@Service
public class GpsSyncServiceImpl implements GpsSyncService {

    private static final Logger logger = LoggerFactory.getLogger(GpsSyncServiceImpl.class);

    private static final String STATUS_OK = "OK";
    private static final String STATUS_SKIPPED = "SKIPPED";
    private static final String STATUS_FAILED = "FAILED";

    private static final String REPORT_FUEL = "fuel";
    private static final String REPORT_SPEED = "speed";
    private static final String REPORT_ENGINE = "engine";

    /** Guards against a scheduled run overlapping a manually triggered one. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final WialonProperties properties;
    private final WialonClient client;
    private final WialonRowMapper rowMapper;
    private final TruckResolver truckResolver;
    private final GpsPersistenceService persistenceService;

    public GpsSyncServiceImpl(WialonProperties properties,
                              WialonClient client,
                              WialonRowMapper rowMapper,
                              TruckResolver truckResolver,
                              GpsPersistenceService persistenceService) {
        this.properties = properties;
        this.client = client;
        this.rowMapper = rowMapper;
        this.truckResolver = truckResolver;
        this.persistenceService = persistenceService;
    }

    @Override
    public GpsSyncResultDto sync() {
        LocalDateTime to = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime from = to.minusHours(properties.getWindowHours());
        return sync(from, to);
    }

    @Override
    public GpsSyncResultDto sync(LocalDateTime from, LocalDateTime to) {
        LocalDateTime startedAt = LocalDateTime.now();
        List<GpsReportSyncResult> results = new ArrayList<>();

        if (!properties.isEnabled()) {
            return finish(startedAt, from, to, List.of(GpsReportSyncResult.builder()
                    .report("all")
                    .status(STATUS_SKIPPED)
                    .message("Wialon integration is disabled (wialon.enabled=false)")
                    .build()));
        }
        if (from == null || to == null || !from.isBefore(to)) {
            return finish(startedAt, from, to, List.of(GpsReportSyncResult.builder()
                    .report("all")
                    .status(STATUS_FAILED)
                    .message("Invalid window: from must be before to")
                    .build()));
        }
        if (!running.compareAndSet(false, true)) {
            return finish(startedAt, from, to, List.of(GpsReportSyncResult.builder()
                    .report("all")
                    .status(STATUS_SKIPPED)
                    .message("A GPS sync is already running")
                    .build()));
        }

        String sid = null;
        try {
            long fromEpoch = toEpochSeconds(from);
            long toEpoch = toEpochSeconds(to);
            logger.info("GPS sync window {} .. {} (epoch {} .. {})", from, to, fromEpoch, toEpoch);

            WialonSession session = client.login();
            sid = session.getEid();

            client.setLocale(sid);
            settle();

            List<WialonResourceItem> resources = client.searchResources(sid);
            TruckResolver.Snapshot trucks = truckResolver.snapshot(properties.getCompanyRefId());

            results.add(syncFillingReport(REPORT_FUEL, properties.getReports().getFuel(),
                    sid, resources, trucks, fromEpoch, toEpoch, true));
            results.add(syncFillingReport(REPORT_SPEED, properties.getReports().getSpeed(),
                    sid, resources, trucks, fromEpoch, toEpoch, false));
            results.add(syncEngineReport(properties.getReports().getEngine(),
                    sid, resources, trucks, fromEpoch, toEpoch));

        } catch (RuntimeException ex) {
            logger.error("GPS sync aborted: {}", ex.getMessage(), ex);
            results.add(GpsReportSyncResult.builder()
                    .report("all")
                    .status(STATUS_FAILED)
                    .message(ex.getMessage())
                    .build());
        } finally {
            if (sid != null) {
                client.logout(sid);
            }
            running.set(false);
        }

        return finish(startedAt, from, to, results);
    }

    // ------------------------------------------------------------ fuel / speed

    /**
     * Runs one of the two group reports that share the fillings column layout and
     * writes the rows.
     *
     * @param intoFuelFillings true writes to FuelFillings, false to SpeedReport
     */
    private GpsReportSyncResult syncFillingReport(String name,
                                                 WialonProperties.ReportConfig config,
                                                 String sid,
                                                 List<WialonResourceItem> resources,
                                                 TruckResolver.Snapshot trucks,
                                                 long fromEpoch,
                                                 long toEpoch,
                                                 boolean intoFuelFillings) {
        GpsReportSyncResult.GpsReportSyncResultBuilder result =
                GpsReportSyncResult.builder().report(name);

        if (!config.isEnabled()) {
            return result.status(STATUS_SKIPPED).message("Report disabled in configuration").build();
        }

        try {
            long resourceId = resolveResourceId(config, resources);
            int templateId = resolveTemplateId(config, resources);
            long objectId = requireObjectId(config, name);

            // A session holds one report result at a time; the previous one must go
            // before the next exec_report, which the legacy job never did.
            client.cleanupResult(sid);
            WialonReportExecResponse response =
                    client.execReport(sid, resourceId, templateId, objectId, fromEpoch, toEpoch);

            TableRef table = findTable(response, config.getTable());
            if (table == null) {
                return result.status(STATUS_SKIPPED)
                        .message("Report produced no table named " + config.getTable())
                        .build();
            }

            List<WialonResultRow> leaves =
                    rowMapper.flatten(client.selectResultRows(sid, table.index(), table.rows()));
            result.rowsFetched(leaves.size());

            int skippedZeroCount = 0;
            int skippedNoTime = 0;
            int skippedNoTruck = 0;
            Map<Integer, List<GpsFillingRow>> byTruck = new LinkedHashMap<>();

            for (WialonResultRow leaf : leaves) {
                GpsFillingRow row = rowMapper.toFillingRow(leaf);
                if (row == null) {
                    skippedNoTime++;
                    continue;
                }
                // Legacy behaviour: a zero event count means the report printed a
                // placeholder line for a vehicle with nothing to report.
                if ("0".equals(row.getCount())) {
                    skippedZeroCount++;
                    continue;
                }
                if (row.getTime() == null) {
                    skippedNoTime++;
                    continue;
                }
                Optional<TruckMaster> truck = trucks.resolve(row.getVehicle());
                if (truck.isEmpty()) {
                    skippedNoTruck++;
                    continue;
                }
                byTruck.computeIfAbsent(truck.get().getId(), key -> new ArrayList<>()).add(row);
            }

            int written = intoFuelFillings
                    ? persistenceService.upsertFuelFillings(properties.getCompanyRefId(), byTruck)
                    : persistenceService.upsertSpeedReports(properties.getCompanyRefId(), byTruck);

            if (skippedNoTruck > 0) {
                logger.warn("{} report: {} rows had no matching active truck", name, skippedNoTruck);
            }

            return result.status(STATUS_OK)
                    .rowsWritten(written)
                    .rowsSkippedNoTruck(skippedNoTruck)
                    .rowsSkippedNoTime(skippedNoTime)
                    .rowsSkippedZeroCount(skippedZeroCount)
                    .build();

        } catch (RuntimeException ex) {
            logger.error("{} report sync failed: {}", name, ex.getMessage(), ex);
            return result.status(STATUS_FAILED).message(ex.getMessage()).build();
        }
    }

    // ---------------------------------------------------------------- engine

    /**
     * The engine-hours report runs once per unit, so the truck is known up front
     * and no name matching is needed on the rows themselves.
     */
    private GpsReportSyncResult syncEngineReport(WialonProperties.ReportConfig config,
                                                 String sid,
                                                 List<WialonResourceItem> resources,
                                                 TruckResolver.Snapshot trucks,
                                                 long fromEpoch,
                                                 long toEpoch) {
        GpsReportSyncResult.GpsReportSyncResultBuilder result =
                GpsReportSyncResult.builder().report(REPORT_ENGINE);

        if (!config.isEnabled()) {
            return result.status(STATUS_SKIPPED).message("Report disabled in configuration").build();
        }

        try {
            long resourceId = resolveResourceId(config, resources);
            int templateId = resolveTemplateId(config, resources);
            List<WialonUnitItem> units = client.searchUnits(sid);
            logger.info("Engine hours: {} Wialon units to process", units.size());

            int fetched = 0;
            int written = 0;
            int skippedNoTruck = 0;
            int skippedNoTime = 0;
            List<String> failures = new ArrayList<>();

            for (WialonUnitItem unit : units) {
                Optional<TruckMaster> truck = trucks.resolve(unit.getName());
                if (truck.isEmpty()) {
                    skippedNoTruck++;
                    continue;
                }
                try {
                    client.cleanupResult(sid);
                    WialonReportExecResponse response = client.execReport(
                            sid, resourceId, templateId, unit.getId(), fromEpoch, toEpoch);

                    TableRef table = findTable(response, config.getTable());
                    if (table == null) {
                        continue;
                    }

                    List<WialonResultRow> leaves = rowMapper.flatten(
                            client.selectResultRows(sid, table.index(), table.rows()));
                    fetched += leaves.size();

                    List<GpsEngineHourRow> rows = new ArrayList<>();
                    for (WialonResultRow leaf : leaves) {
                        GpsEngineHourRow row = rowMapper.toEngineHourRow(leaf);
                        if (row == null || row.getBeginTime() == null) {
                            skippedNoTime++;
                            continue;
                        }
                        rows.add(row);
                    }

                    written += persistenceService.upsertEngineHours(
                            properties.getCompanyRefId(), truck.get().getId(), rows);

                } catch (RuntimeException ex) {
                    // One unhealthy unit must not cost the rest of the fleet.
                    logger.warn("Engine hours failed for unit {}: {}", unit.getName(), ex.getMessage());
                    failures.add(unit.getName());
                }
            }

            if (skippedNoTruck > 0) {
                logger.warn("Engine hours: {} Wialon units had no matching active truck", skippedNoTruck);
            }

            return result.status(STATUS_OK)
                    .rowsFetched(fetched)
                    .rowsWritten(written)
                    .rowsSkippedNoTruck(skippedNoTruck)
                    .rowsSkippedNoTime(skippedNoTime)
                    .message(failures.isEmpty() ? null : "Units that failed: " + String.join(", ", failures))
                    .build();

        } catch (RuntimeException ex) {
            logger.error("Engine hours sync failed: {}", ex.getMessage(), ex);
            return result.status(STATUS_FAILED).message(ex.getMessage()).build();
        }
    }

    // --------------------------------------------------------------- helpers

    /** A report table together with the index select_result_rows needs. */
    private record TableRef(int index, int rows) { }

    private TableRef findTable(WialonReportExecResponse response, String tableName) {
        if (response == null || response.getReportResult() == null
                || response.getReportResult().getTables() == null || tableName == null) {
            return null;
        }
        List<WialonReportTable> tables = response.getReportResult().getTables();
        for (int i = 0; i < tables.size(); i++) {
            WialonReportTable table = tables.get(i);
            if (table != null && tableName.equals(table.getName())) {
                int rows = table.getRows() == null ? 0 : table.getRows();
                return new TableRef(i, rows);
            }
        }
        return null;
    }

    /**
     * Uses the configured resource id when present, otherwise the first
     * avl_resource - which is what the legacy engine report did.
     */
    private long resolveResourceId(WialonProperties.ReportConfig config,
                                   List<WialonResourceItem> resources) {
        if (config.getResourceId() != null) {
            return config.getResourceId();
        }
        if (resources.isEmpty()) {
            throw new WialonApiException("No avl_resource available to run the report");
        }
        return resources.get(0).getId();
    }

    /**
     * Template name wins over the numeric id: names survive Wialon renumbering,
     * which the hardcoded ids in the legacy code did not.
     */
    private int resolveTemplateId(WialonProperties.ReportConfig config,
                                  List<WialonResourceItem> resources) {
        if (config.getTemplateName() != null && !config.getTemplateName().isBlank()) {
            for (WialonResourceItem resource : resources) {
                if (resource.getReports() == null) {
                    continue;
                }
                for (Map.Entry<String, WialonReportTemplate> entry : resource.getReports().entrySet()) {
                    WialonReportTemplate template = entry.getValue();
                    if (template != null && config.getTemplateName().equals(template.getName())) {
                        return template.getId() != null
                                ? template.getId()
                                : Integer.parseInt(entry.getKey());
                    }
                }
            }
            throw new WialonApiException(
                    "No Wialon report template named '" + config.getTemplateName() + "'");
        }
        if (config.getTemplateId() == null) {
            throw new WialonApiException("Neither template-name nor template-id is configured");
        }
        return config.getTemplateId();
    }

    private long requireObjectId(WialonProperties.ReportConfig config, String reportName) {
        if (config.getObjectId() == null) {
            throw new WialonApiException("object-id is not configured for the " + reportName + " report");
        }
        return config.getObjectId();
    }

    /**
     * Converts the window bounds to the epoch seconds Wialon expects.
     *
     * Wialon treats {@code interval.from/to} as true UTC instants, so a bound
     * given in Malaysian wall-clock time must be shifted by the configured
     * offset ({@code wialon.locale.tz-base-seconds}). The configured offset is
     * used rather than the JVM default zone so the result does not depend on
     * where the server happens to run.
     *
     * With legacyEpoch enabled this instead reproduces the .NET
     * {@code DateTime.Now - 1970-01-01}, which sent local time as if it were
     * UTC: an interval eight hours late, which is why the old job never saw
     * fillings between midnight and 08:00.
     */
    private long toEpochSeconds(LocalDateTime value) {
        if (properties.isLegacyEpoch()) {
            return value.toEpochSecond(ZoneOffset.UTC);
        }
        return value.toEpochSecond(ZoneOffset.ofTotalSeconds(properties.getLocale().getTzBaseSeconds()));
    }

    /** Mirrors the legacy pause after set_locale before the first report runs. */
    private void settle() {
        int seconds = properties.getLocaleSettleSeconds();
        if (seconds <= 0) {
            return;
        }
        try {
            logger.info("Waiting {}s for the Wialon locale to settle", seconds);
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private GpsSyncResultDto finish(LocalDateTime startedAt,
                                    LocalDateTime from,
                                    LocalDateTime to,
                                    List<GpsReportSyncResult> results) {
        // A run in which nothing executed - integration disabled, another sync
        // holding the lock, every report switched off - is not a success. It used
        // to be, and the fuel entry screen told the user "GPS data fetched" while
        // the backend had done nothing.
        boolean failed = results.stream().anyMatch(r -> STATUS_FAILED.equals(r.getStatus()));
        boolean ran = results.stream().anyMatch(r -> STATUS_OK.equals(r.getStatus()));
        boolean success = ran && !failed;
        return GpsSyncResultDto.builder()
                .startedAt(startedAt)
                .finishedAt(LocalDateTime.now())
                .windowFrom(from)
                .windowTo(to)
                .companyRefId(properties.getCompanyRefId())
                .reports(results)
                .success(success)
                .build();
    }
}
