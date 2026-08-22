package my.maleva.api.module.gps.service.impl;

import my.maleva.api.module.fleet.entity.EngineHours;
import my.maleva.api.module.fleet.entity.FuelFillings;
import my.maleva.api.module.fleet.entity.SpeedReport;
import my.maleva.api.module.gps.dto.GpsEngineHourRow;
import my.maleva.api.module.gps.dto.GpsFillingRow;
import my.maleva.api.module.gps.repository.GpsEngineHoursRepository;
import my.maleva.api.module.gps.repository.GpsFuelFillingRepository;
import my.maleva.api.module.gps.repository.GpsSpeedReportRepository;
import my.maleva.api.module.gps.service.GpsPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Persists synced GPS rows.
 *
 * The legacy job built INSERT and DELETE statements by string concatenation from
 * values Wialon supplied (vehicle, location, driver), which is both injectable
 * and fragile around apostrophes in place names. Everything here goes through
 * JPA with bound parameters.
 */
@Service
public class GpsPersistenceServiceImpl implements GpsPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(GpsPersistenceServiceImpl.class);

    // Column limits from the SQL Server schema, in characters.
    private static final int LEN_VEHICLE = 200;
    private static final int LEN_LOCATION = 200;
    private static final int LEN_SHORT = 100;
    private static final int LEN_DRIVER = 200;
    private static final int LEN_ENGINE_LOCATION = 800;

    private final GpsFuelFillingRepository fuelFillingRepository;
    private final GpsSpeedReportRepository speedReportRepository;
    private final GpsEngineHoursRepository engineHoursRepository;

    public GpsPersistenceServiceImpl(GpsFuelFillingRepository fuelFillingRepository,
                                     GpsSpeedReportRepository speedReportRepository,
                                     GpsEngineHoursRepository engineHoursRepository) {
        this.fuelFillingRepository = fuelFillingRepository;
        this.speedReportRepository = speedReportRepository;
        this.engineHoursRepository = engineHoursRepository;
    }

    @Override
    @Transactional
    public int upsertFuelFillings(Integer companyRefId, Map<Integer, List<GpsFillingRow>> byTruck) {
        LocalDateTime now = LocalDateTime.now();
        int written = 0;

        for (Map.Entry<Integer, List<GpsFillingRow>> entry : byTruck.entrySet()) {
            Integer truckRefId = entry.getKey();
            for (GpsFillingRow row : entry.getValue()) {
                fuelFillingRepository.deleteForUpsert(companyRefId, truckRefId, row.getTime());
                fuelFillingRepository.save(FuelFillings.builder()
                        .companyRefId(companyRefId)
                        .truckRefId(truckRefId)
                        .vehicle(truncate(row.getVehicle(), LEN_VEHICLE))
                        .time(row.getTime())
                        .location(truncate(row.getLocation(), LEN_LOCATION))
                        .count(truncate(row.getCount(), LEN_SHORT))
                        .filled(truncate(row.getFilled(), LEN_SHORT))
                        .driver(truncate(row.getDriver(), LEN_DRIVER))
                        .createdDate(now)
                        .build());
                written++;
            }
        }
        logger.info("Wrote {} fuel filling rows for company {}", written, companyRefId);
        return written;
    }

    @Override
    @Transactional
    public int upsertSpeedReports(Integer companyRefId, Map<Integer, List<GpsFillingRow>> byTruck) {
        LocalDateTime now = LocalDateTime.now();
        int written = 0;

        for (Map.Entry<Integer, List<GpsFillingRow>> entry : byTruck.entrySet()) {
            Integer truckRefId = entry.getKey();
            for (GpsFillingRow row : entry.getValue()) {
                speedReportRepository.deleteForUpsert(companyRefId, truckRefId, row.getTime());
                speedReportRepository.save(SpeedReport.builder()
                        .companyRefId(companyRefId)
                        .truckRefId(truckRefId)
                        .vehicle(truncate(row.getVehicle(), LEN_VEHICLE))
                        .time(row.getTime())
                        .location(truncate(row.getLocation(), LEN_LOCATION))
                        .count(truncate(row.getCount(), LEN_SHORT))
                        .filled(truncate(row.getFilled(), LEN_SHORT))
                        .driver(truncate(row.getDriver(), LEN_DRIVER))
                        .createdDate(now)
                        .build());
                written++;
            }
        }
        logger.info("Wrote {} speed report rows for company {}", written, companyRefId);
        return written;
    }

    @Override
    @Transactional
    public int upsertEngineHours(Integer companyRefId, Integer truckRefId, List<GpsEngineHourRow> rows) {
        LocalDateTime now = LocalDateTime.now();
        int written = 0;

        for (GpsEngineHourRow row : rows) {
            engineHoursRepository.deleteForUpsert(companyRefId, truckRefId, row.getBeginTime());
            engineHoursRepository.save(EngineHours.builder()
                    .companyRefId(companyRefId)
                    .truckRefId(truckRefId)
                    .beginTime(row.getBeginTime())
                    // endTime is NOT NULL in the schema; fall back to the start of the interval
                    .endTime(row.getEndTime() != null ? row.getEndTime() : row.getBeginTime())
                    .beginLocation(truncate(row.getBeginLocation(), LEN_ENGINE_LOCATION))
                    .endLocation(truncate(row.getEndLocation(), LEN_ENGINE_LOCATION))
                    .totalTime(truncate(row.getTotalTime(), LEN_SHORT))
                    .inMotion(truncate(row.getInMotion(), LEN_SHORT))
                    .idling(truncate(row.getIdling(), LEN_SHORT))
                    .mileage(truncate(row.getMileage(), LEN_SHORT))
                    .consumedbyFLSinidlerun(truncate(row.getConsumedByFlsInIdleRun(), LEN_SHORT))
                    .createdDate(now)
                    .build());
            written++;
        }
        if (written > 0) {
            logger.debug("Wrote {} engine hour rows for truck {}", written, truckRefId);
        }
        return written;
    }

    /**
     * Wialon location strings routinely exceed the column widths. The legacy job
     * let the insert fail and swallowed the exception, losing the row silently.
     */
    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        if (value.length() <= max) {
            return value;
        }
        logger.debug("Truncating GPS value from {} to {} characters", value.length(), max);
        return value.substring(0, max);
    }
}
