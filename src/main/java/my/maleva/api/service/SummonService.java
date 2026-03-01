package my.maleva.api.service;

import my.maleva.api.dto.SummonDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * SummonService - Business logic for Summon
 * Incorporates SP_Summon stored procedure logic
 */
public interface SummonService {

    List<SummonDto> getByTruckName(String truckName);

    List<SummonDto> getByDriverName(String driverName);

    List<SummonDto> getByComid(Integer comid);

    List<SummonDto> getByEntryDate(LocalDate entryDate);

    List<SummonDto> getByEntryDateRange(LocalDate startDate, LocalDate endDate);

    List<SummonDto> getByAmountRange(BigDecimal minAmount, BigDecimal maxAmount);

    List<SummonDto> getByCountry(String country);

    Optional<SummonDto> getById(Integer id);

    Optional<SummonDto> getByTruckAndDriver(String truckName, String driverName);

    SummonDto create(SummonDto dto);

    SummonDto update(Integer id, SummonDto dto);

    boolean delete(Integer id);

    long countByComid(Integer comid);

    long countByCountry(String country);

    void validateSummonData(SummonDto dto);

    SummonDto processSummon(SummonDto dto, Integer comid);
}

