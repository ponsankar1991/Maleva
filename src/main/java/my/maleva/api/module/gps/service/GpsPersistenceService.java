package my.maleva.api.module.gps.service;

import my.maleva.api.module.gps.dto.GpsEngineHourRow;
import my.maleva.api.module.gps.dto.GpsFillingRow;

import java.util.List;
import java.util.Map;

/**
 * Writes parsed Wialon rows into the FuelFillings, SpeedReport and EngineHours
 * tables.
 *
 * Kept apart from {@link GpsSyncService} so that no database transaction is held
 * open across a Wialon HTTP call - report execution can take minutes.
 */
public interface GpsPersistenceService {

    /**
     * Upserts fuel fillings. Existing rows with the same company, truck and time
     * are deleted first, so re-running a window overwrites instead of duplicating.
     *
     * @param byTruck parsed rows grouped by resolved TruckMaster id
     * @return number of rows written
     */
    int upsertFuelFillings(Integer companyRefId, Map<Integer, List<GpsFillingRow>> byTruck);

    /** Upserts speeding events. Same shape as the fillings table. */
    int upsertSpeedReports(Integer companyRefId, Map<Integer, List<GpsFillingRow>> byTruck);

    /**
     * Upserts engine-hours intervals for one truck. The engine report runs per
     * unit, so the truck is known before the rows are parsed.
     *
     * @return number of rows written
     */
    int upsertEngineHours(Integer companyRefId, Integer truckRefId, List<GpsEngineHourRow> rows);
}
