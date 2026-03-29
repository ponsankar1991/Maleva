package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.TollEntryDto;
import my.maleva.api.module.fleet.dto.TollEntryDetailsDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TollEntryService - Business logic for TollEntry
 * Incorporates SP_TollEntry stored procedure logic
 */
public interface TollEntryService {

    List<TollEntryDto> getByCompanyRefId(Integer companyRefId);

    List<TollEntryDto> getActiveByCompanyRefId(Integer companyRefId);

    Optional<TollEntryDto> getByCNumber(Integer cNumber, Integer companyRefId);

    List<TollEntryDto> getByUserRefId(Integer userRefId);

    List<TollEntryDto> getByEmployeeRefId(Integer employeeRefId);

    List<TollEntryDto> getByTruckRefid(Integer truckRefid);

    List<TollEntryDto> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<TollEntryDto> getByCompanyAndDateRange(Integer companyRefId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<TollEntryDto> getById(Integer id);

    TollEntryDto create(TollEntryDto dto);

    TollEntryDto update(Integer id, TollEntryDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countActiveByCompanyRefId(Integer companyRefId);

    void validateTollEntryData(TollEntryDto dto);

    TollEntryDto activateTollEntry(Integer id);

    TollEntryDto deactivateTollEntry(Integer id);

    TollEntryDto processTollEntry(TollEntryDto dto, List<TollEntryDetailsDto> details, Integer companyId);
}

