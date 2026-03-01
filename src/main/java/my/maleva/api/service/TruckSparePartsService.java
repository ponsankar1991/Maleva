package my.maleva.api.service;

import my.maleva.api.dto.TruckSparePartsDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TruckSparePartsService - Business logic for TruckSpareParts
 * Incorporates SP_TruckSpareParts stored procedure logic
 */
public interface TruckSparePartsService {

    List<TruckSparePartsDto> getByComid(Integer comid);

    List<TruckSparePartsDto> getByTruckName(String truckName);

    List<TruckSparePartsDto> getByTruckNameAndComid(String truckName, Integer comid);

    List<TruckSparePartsDto> getByDriverName(String driverName);

    List<TruckSparePartsDto> getByDriverNameAndComid(String driverName, Integer comid);

    List<TruckSparePartsDto> getBySpareParts(String spareParts);

    List<TruckSparePartsDto> getByDateRange(LocalDate startDate, LocalDate endDate);

    List<TruckSparePartsDto> getByComidAndDateRange(Integer comid, LocalDate startDate, LocalDate endDate);

    Optional<TruckSparePartsDto> getById(Integer id);

    TruckSparePartsDto create(TruckSparePartsDto dto);

    TruckSparePartsDto update(Integer id, TruckSparePartsDto dto);

    boolean delete(Integer id);

    long countByComid(Integer comid);

    long countByTruckNameAndComid(String truckName, Integer comid);

    void validateTruckSparePartsData(TruckSparePartsDto dto);

    TruckSparePartsDto processTruckSpareParts(TruckSparePartsDto dto, Integer comid);
}

