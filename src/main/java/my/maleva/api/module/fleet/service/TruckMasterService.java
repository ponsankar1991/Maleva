package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.TruckDriverContactDto;
import my.maleva.api.module.fleet.dto.TruckMasterDto;
import my.maleva.api.common.dto.ComboListModel;
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

    /**
     * Get Truck combo list for dropdown/UI
     * Equivalent to .NET GetTruck method
     * 
     * @param companyId Company ID (required)
     * @param truckType Truck type filter (optional, null for all types)
     * @return List of ComboListModel with Id and TruckName as AccountName
     */
    List<ComboListModel> getTruckCombo(Integer companyId, String truckType);

    void validateTruckMasterData(TruckMasterDto dto);

    TruckMasterDto activateTruck(Integer id);

    TruckMasterDto deactivateTruck(Integer id);

    boolean existsByTruckNumber(String truckNumber, Integer companyRefId);

    TruckMasterDto processTruck(TruckMasterDto dto, Integer companyId);

    /**
     * Sets, moves or clears the driver who normally drives this truck.
     *
     * <p>Writes {@code DriverMaster.TruckRefId} - the one place the pairing is
     * kept - so the truck screen and the driver screen can never disagree. Any
     * other driver still pointing at this truck is released, because one truck
     * has one default driver.
     *
     * @param driverRefId the driver, or null / 0 to leave the truck without one
     */
    TruckMasterDto assignDriver(Integer truckId, Integer driverRefId);

    /**
     * Puts a truck on the road, in the workshop, or marks it sold.
     *
     * <p>Nothing is deleted: the truck keeps every order, RTI and report it has
     * ever been on. A WORKSHOP truck is simply not offered by the Truck Order
     * Calendar until {@code workshopUntil} has passed; SOLD is never offered.
     *
     * @param workshopUntil the day it is expected back - kept only for WORKSHOP,
     *                      and cleared for the other two so a stale date cannot
     *                      quietly bring a sold truck back
     */
    TruckMasterDto updateStatus(Integer truckId, String truckStatus, java.time.LocalDate workshopUntil);

    /**
     * The bookable fleet with the driver of each truck and their phone number -
     * what the header's truck/driver window shows.
     *
     * <p>Only trucks the calendar books: active, ours, and flagged orderable.
     * Ordered by plate, so the list reads the same every time it is opened.
     */
    List<TruckDriverContactDto> getFleetDrivers(Integer companyRefId);

    /**
     * Search trucks with optional keyword, column filter, type and pagination.
     * @param companyId company id
     * @param startIndex offset (0-based). If -1, return last page start offset.
     * @param pageCount number of records per page (if <=0 then all records returned)
     * @param keyword search keyword
     * @param column column to search (TruckName, Id, All)
     * @param type optional truck type filter
     * @return SearchResultDto containing list and total count
     */
    my.maleva.api.module.fleet.dto.SearchResultDto searchTrucks(Integer companyId, Integer startIndex, Integer pageCount, String keyword, String column, String type);

    java.util.List<my.maleva.api.module.fleet.dto.TruckMasterDto> getAllTruckDetailCombo(Integer companyId, String keyword, String column, String type);
}
