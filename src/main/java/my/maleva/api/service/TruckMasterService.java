package my.maleva.api.service;

import my.maleva.api.dto.TruckMasterDto;
import java.util.List;
import java.util.Optional;

/**
 * TruckMasterService - Business logic for TruckMaster
 * Incorporates SP_Truck stored procedure logic
 */
public interface TruckMasterService {

    List<TruckMasterDto> getByCompanyRefId(Integer companyRefId);

    List<TruckMasterDto> getActiveByCompanyRefId(Integer companyRefId);

    Optional<TruckMasterDto> getByTruckName(String truckName, Integer companyRefId);

    Optional<TruckMasterDto> getByTruckNumber(String truckNumber, Integer companyRefId);

    Optional<TruckMasterDto> getByCNumber(Integer cNumber, Integer companyRefId);

    List<TruckMasterDto> getByTruckType(String truckType);

    List<TruckMasterDto> getByCompanyAndTruckType(Integer companyRefId, String truckType);

    List<TruckMasterDto> getByVehicleType(String vehicleType);

    Optional<TruckMasterDto> getById(Integer id);

    TruckMasterDto create(TruckMasterDto dto);

    TruckMasterDto update(Integer id, TruckMasterDto dto);

    boolean delete(Integer id);

    long countByCompanyRefId(Integer companyRefId);

    long countActiveByCompanyRefId(Integer companyRefId);

    void validateTruckMasterData(TruckMasterDto dto);

    TruckMasterDto activateTruck(Integer id);

    TruckMasterDto deactivateTruck(Integer id);

    boolean existsByTruckNumber(String truckNumber, Integer companyRefId);

    TruckMasterDto processTruck(TruckMasterDto dto, Integer companyId);
}

