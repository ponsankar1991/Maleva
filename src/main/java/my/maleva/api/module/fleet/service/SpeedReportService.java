package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.SpeedReportDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SpeedReportService - Business logic for SpeedReport
 */
public interface SpeedReportService {

    List<SpeedReportDto> getByCompanyRefId(Integer companyRefId);

    List<SpeedReportDto> getByTruckRefId(Integer truckRefId);

    List<SpeedReportDto> getByCompanyAndTruck(Integer companyRefId, Integer truckRefId);

    List<SpeedReportDto> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    List<SpeedReportDto> getByCompanyAndTimeRange(Integer companyRefId, LocalDateTime startTime, LocalDateTime endTime);

    Optional<SpeedReportDto> getById(Integer id);

    SpeedReportDto create(SpeedReportDto dto);

    SpeedReportDto update(Integer id, SpeedReportDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countByTruckRefId(Integer truckRefId);

    void validateSpeedReportData(SpeedReportDto dto);
}

