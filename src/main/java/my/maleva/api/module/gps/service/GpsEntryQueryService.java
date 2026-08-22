package my.maleva.api.module.gps.service;

import my.maleva.api.module.gps.dto.GpsEngineHoursDto;
import my.maleva.api.module.gps.dto.GpsFuelFillingDto;
import my.maleva.api.module.gps.dto.GpsSpeedReportDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Read side of the GPS module.
 *
 * Replaces the legacy GPSEntryServices.SelectFuelFillings / SelectSpeedReport /
 * SelectEngineHours, which built their WHERE clause by string concatenation from
 * request values.
 *
 * The legacy Insert* endpoints on GPSEntryController are deliberately not ported:
 * they called SP_FuelFillings, SP_EngineHours and SP_SpeedReport, none of which
 * exist in the database. The sync job is the only writer.
 */
public interface GpsEntryQueryService {

    /**
     * Fuel fillings within a window.
     *
     * @param truckRefId optional filter; null returns every truck of the company
     */
    List<GpsFuelFillingDto> findFuelFillings(Integer companyRefId,
                                             Integer truckRefId,
                                             LocalDateTime from,
                                             LocalDateTime to);

    /** Speeding events within a window. */
    List<GpsSpeedReportDto> findSpeedReports(Integer companyRefId,
                                             Integer truckRefId,
                                             LocalDateTime from,
                                             LocalDateTime to);

    /** Engine-hours intervals whose start falls within the window. */
    List<GpsEngineHoursDto> findEngineHours(Integer companyRefId,
                                            Integer truckRefId,
                                            LocalDateTime from,
                                            LocalDateTime to);
}
