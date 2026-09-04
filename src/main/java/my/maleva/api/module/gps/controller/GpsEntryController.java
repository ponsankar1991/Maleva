package my.maleva.api.module.gps.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.gps.dto.GpsEngineHoursDto;
import my.maleva.api.module.gps.dto.GpsFuelFillingDto;
import my.maleva.api.module.gps.dto.GpsFuelMatchDto;
import my.maleva.api.module.gps.dto.GpsReportSyncResult;
import my.maleva.api.module.gps.dto.GpsSpeedReportDto;
import my.maleva.api.module.gps.dto.GpsSyncResultDto;
import my.maleva.api.module.gps.service.FuelGpsMatchService;
import my.maleva.api.module.gps.service.GpsEntryQueryService;
import my.maleva.api.module.gps.service.GpsSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * GPS data captured from Wialon.
 *
 * Replaces the legacy GPSEntryController. The three Select* actions become GET
 * endpoints; the three Insert* actions are gone, because they called stored
 * procedures that do not exist in the database and nothing ever wrote through
 * them - the sync job is the only writer, and it is exposed here as
 * POST /api/gps/sync.
 */
@RestController
@RequestMapping("/api/gps")
@Validated
@PermitAll
public class GpsEntryController {

    private static final Logger logger = LoggerFactory.getLogger(GpsEntryController.class);

    private final GpsEntryQueryService queryService;
    private final GpsSyncService syncService;
    private final FuelGpsMatchService matchService;

    public GpsEntryController(GpsEntryQueryService queryService,
                              GpsSyncService syncService,
                              FuelGpsMatchService matchService) {
        this.queryService = queryService;
        this.syncService = syncService;
        this.matchService = matchService;
    }

    /**
     * Fuel fillings recorded by the on-board fuel sensor.
     * Legacy equivalent: POST /GPSEntry/SelectFuelFillings
     */
    @GetMapping("/fuel-fillings")
    public ResponseEntity<ApiResponse<List<GpsFuelFillingDto>>> getFuelFillings(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) Integer truckRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<GpsFuelFillingDto> data =
                queryService.findFuelFillings(companyRefId, truckRefId, from, to);
        return ResponseEntity.ok(ApiResponse.success(data, "Fuel fillings retrieved"));
    }

    /**
     * Speeding events.
     * Legacy equivalent: POST /GPSEntry/SelectSpeedReport
     */
    @GetMapping("/speed-reports")
    public ResponseEntity<ApiResponse<List<GpsSpeedReportDto>>> getSpeedReports(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) Integer truckRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<GpsSpeedReportDto> data =
                queryService.findSpeedReports(companyRefId, truckRefId, from, to);
        return ResponseEntity.ok(ApiResponse.success(data, "Speed reports retrieved"));
    }

    /**
     * Engine-hours intervals.
     * Legacy equivalent: POST /GPSEntry/SelectEngineHours
     */
    @GetMapping("/engine-hours")
    public ResponseEntity<ApiResponse<List<GpsEngineHoursDto>>> getEngineHours(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) Integer truckRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<GpsEngineHoursDto> data =
                queryService.findEngineHours(companyRefId, truckRefId, from, to);
        return ResponseEntity.ok(ApiResponse.success(data, "Engine hours retrieved"));
    }

    /**
     * The GPS filling assigned to each fuel entry of one truck on one day.
     *
     * Each filling is given to at most one entry, so a truck that refuelled
     * three times produces three distinct matches rather than the same filling
     * repeated. Entries with no match carry the reason why.
     */
    @GetMapping("/fuel-match")
    public ResponseEntity<ApiResponse<List<GpsFuelMatchDto>>> getFuelMatch(
            @RequestParam Integer companyRefId,
            @RequestParam Integer truckRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate saleDate) {

        List<GpsFuelMatchDto> data =
                matchService.matchForTruckOnDay(companyRefId, truckRefId, saleDate);
        return ResponseEntity.ok(ApiResponse.success(data, "GPS fuel match resolved"));
    }

    /**
     * Pins one fuel entry to one GPS filling by hand.
     *
     * For the days where the litres alone cannot decide - a truck that refuelled
     * three times with similar volumes. A manual choice is recorded as MANUAL
     * and the matcher never overwrites it. Passing no fuelFillingId clears the
     * link and hands the decision back to the matcher.
     */
    @PutMapping("/fuel-match/{fuelEntryId}")
    public ResponseEntity<ApiResponse<GpsFuelMatchDto>> setManualMatch(
            @PathVariable Integer fuelEntryId,
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) Integer fuelFillingId) {

        GpsFuelMatchDto data = matchService.setManualMatch(companyRefId, fuelEntryId, fuelFillingId);
        logger.info("Fuel entry {} manually matched to filling {}", fuelEntryId, fuelFillingId);
        return ResponseEntity.ok(ApiResponse.success(data, "GPS filling assigned"));
    }

    /**
     * Pulls from Wialon now. Without a window it uses the configured lookback.
     * Runs inline and can take minutes, since Wialon report execution is slow.
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<GpsSyncResultDto>> sync(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        logger.info("Manual GPS sync requested, window {} .. {}", from, to);
        GpsSyncResultDto result = (from == null || to == null)
                ? syncService.sync()
                : syncService.sync(from, to);

        // Nothing executed at all - integration switched off, another sync holding
        // the lock, or the login itself failed. That is not a 200: the caller must
        // see why, instead of the "GPS data fetched" the screen used to show.
        boolean nothingRan = result.getReports().stream()
                .noneMatch(r -> "OK".equals(r.getStatus()));
        if (nothingRan) {
            String reason = result.getReports().stream()
                    .map(GpsReportSyncResult::getMessage)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("no report ran");
            logger.warn("Manual GPS sync did not run: {}", reason);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("GPS sync did not run: " + reason,
                            HttpStatus.SERVICE_UNAVAILABLE.value()));
        }

        return ResponseEntity.ok(result.isSuccess()
                ? ApiResponse.success(result, "GPS sync completed")
                : ApiResponse.<GpsSyncResultDto>builder()
                        .isSuccess(false)
                        .statusCode(207)
                        .message("GPS sync completed with failures")
                        .data1(result)
                        .build());
    }
}
