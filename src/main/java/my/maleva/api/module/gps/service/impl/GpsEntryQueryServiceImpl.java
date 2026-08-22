package my.maleva.api.module.gps.service.impl;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.entity.EngineHours;
import my.maleva.api.module.fleet.entity.FuelFillings;
import my.maleva.api.module.fleet.entity.SpeedReport;
import my.maleva.api.module.fleet.entity.TruckMaster;
import my.maleva.api.module.fleet.repository.TruckMasterRepository;
import my.maleva.api.module.gps.dto.GpsEngineHoursDto;
import my.maleva.api.module.gps.dto.GpsFuelFillingDto;
import my.maleva.api.module.gps.dto.GpsSpeedReportDto;
import my.maleva.api.module.gps.repository.GpsEngineHoursRepository;
import my.maleva.api.module.gps.repository.GpsFuelFillingRepository;
import my.maleva.api.module.gps.repository.GpsSpeedReportRepository;
import my.maleva.api.module.gps.service.GpsEntryQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the synced GPS tables and joins the truck name.
 *
 * The truck names are fetched once per request and applied in memory rather than
 * joined in SQL, so a filling whose truck was later deactivated still comes back
 * - the legacy queries used an INNER JOIN on TruckMaster and silently dropped
 * those rows.
 */
@Service
public class GpsEntryQueryServiceImpl implements GpsEntryQueryService {

    /** Guard against an unbounded scan; EngineHours alone holds six figures of rows. */
    private static final int MAX_WINDOW_DAYS = 366;

    private final GpsFuelFillingRepository fuelFillingRepository;
    private final GpsSpeedReportRepository speedReportRepository;
    private final GpsEngineHoursRepository engineHoursRepository;
    private final TruckMasterRepository truckMasterRepository;

    public GpsEntryQueryServiceImpl(GpsFuelFillingRepository fuelFillingRepository,
                                    GpsSpeedReportRepository speedReportRepository,
                                    GpsEngineHoursRepository engineHoursRepository,
                                    TruckMasterRepository truckMasterRepository) {
        this.fuelFillingRepository = fuelFillingRepository;
        this.speedReportRepository = speedReportRepository;
        this.engineHoursRepository = engineHoursRepository;
        this.truckMasterRepository = truckMasterRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GpsFuelFillingDto> findFuelFillings(Integer companyRefId,
                                                    Integer truckRefId,
                                                    LocalDateTime from,
                                                    LocalDateTime to) {
        validate(companyRefId, from, to);
        List<FuelFillings> rows = truckRefId == null
                ? fuelFillingRepository.findByCompanyRefIdAndTimeBetweenOrderByTimeAsc(companyRefId, from, to)
                : fuelFillingRepository.findByCompanyRefIdAndTruckRefIdAndTimeBetweenOrderByTimeAsc(
                        companyRefId, truckRefId, from, to);

        Map<Integer, String> truckNames = truckNames(companyRefId);
        return rows.stream()
                .map(row -> GpsFuelFillingDto.builder()
                        .id(row.getId())
                        .truckRefId(row.getTruckRefId())
                        .truckName(truckNames.get(row.getTruckRefId()))
                        .vehicle(row.getVehicle())
                        .time(row.getTime())
                        .location(row.getLocation())
                        .count(row.getCount())
                        .filled(row.getFilled())
                        .driver(row.getDriver())
                        .createdDate(row.getCreatedDate())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GpsSpeedReportDto> findSpeedReports(Integer companyRefId,
                                                    Integer truckRefId,
                                                    LocalDateTime from,
                                                    LocalDateTime to) {
        validate(companyRefId, from, to);
        List<SpeedReport> rows = truckRefId == null
                ? speedReportRepository.findByCompanyRefIdAndTimeBetweenOrderByTimeAsc(companyRefId, from, to)
                : speedReportRepository.findByCompanyRefIdAndTruckRefIdAndTimeBetweenOrderByTimeAsc(
                        companyRefId, truckRefId, from, to);

        Map<Integer, String> truckNames = truckNames(companyRefId);
        return rows.stream()
                .map(row -> GpsSpeedReportDto.builder()
                        .id(row.getId())
                        .truckRefId(row.getTruckRefId())
                        .truckName(truckNames.get(row.getTruckRefId()))
                        .vehicle(row.getVehicle())
                        .time(row.getTime())
                        .location(row.getLocation())
                        .count(row.getCount())
                        .filled(row.getFilled())
                        .driver(row.getDriver())
                        .createdDate(row.getCreatedDate())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GpsEngineHoursDto> findEngineHours(Integer companyRefId,
                                                   Integer truckRefId,
                                                   LocalDateTime from,
                                                   LocalDateTime to) {
        validate(companyRefId, from, to);
        List<EngineHours> rows = truckRefId == null
                ? engineHoursRepository.findByCompanyRefIdAndBeginTimeBetweenOrderByBeginTimeAsc(
                        companyRefId, from, to)
                : engineHoursRepository.findByCompanyRefIdAndTruckRefIdAndBeginTimeBetweenOrderByBeginTimeAsc(
                        companyRefId, truckRefId, from, to);

        Map<Integer, String> truckNames = truckNames(companyRefId);
        return rows.stream()
                .map(row -> GpsEngineHoursDto.builder()
                        .id(row.getId())
                        .truckRefId(row.getTruckRefId())
                        .truckName(truckNames.get(row.getTruckRefId()))
                        .beginTime(row.getBeginTime())
                        .endTime(row.getEndTime())
                        .beginLocation(row.getBeginLocation())
                        .endLocation(row.getEndLocation())
                        .totalTime(row.getTotalTime())
                        .inMotion(row.getInMotion())
                        .idling(row.getIdling())
                        .mileage(row.getMileage())
                        .consumedByFlsInIdleRun(row.getConsumedbyFLSinidlerun())
                        .createdDate(row.getCreatedDate())
                        .build())
                .toList();
    }

    // --------------------------------------------------------------- helpers

    private Map<Integer, String> truckNames(Integer companyRefId) {
        Map<Integer, String> names = new HashMap<>();
        for (TruckMaster truck : truckMasterRepository.findByCompanyRefId(companyRefId)) {
            names.put(truck.getId(), truck.getTruckName());
        }
        return names;
    }

    private void validate(Integer companyRefId, LocalDateTime from, LocalDateTime to) {
        if (companyRefId == null) {
            throw new InvalidRequestException("companyRefId is required");
        }
        if (from == null || to == null) {
            throw new InvalidRequestException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new InvalidRequestException("from must not be after to");
        }
        if (from.plusDays(MAX_WINDOW_DAYS).isBefore(to)) {
            throw new InvalidRequestException(
                    "Date range must not exceed " + MAX_WINDOW_DAYS + " days");
        }
    }
}
