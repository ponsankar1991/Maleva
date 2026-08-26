package my.maleva.api.module.fleet.service;

import my.maleva.api.module.fleet.dto.DriverMasterDto;
import my.maleva.api.module.fleet.dto.DriverSearchResultDto;

import java.util.List;

/**
 * DriverMasterService - Business logic interface for DriverMaster operations
 * Includes CRUD operations and search with pagination/filtering
 */
public interface DriverMasterService {

    /**
     * Get all drivers
     */
    List<DriverMasterDto> listAll();

    /**
     * Get driver by ID
     */
    DriverMasterDto getById(Integer id);

    /**
     * Create new driver
     */
    DriverMasterDto create(DriverMasterDto dto);

    /**
     * Update existing driver
     */
    DriverMasterDto update(Integer id, DriverMasterDto dto);

    /**
     * Soft delete a driver (Active = 2, the marker the driver search excludes).
     */
    void delete(Integer id);

    /**
     * Bring a soft-deleted driver back as active.
     */
    DriverMasterDto restore(Integer id);

    /**
     * Insert or update one driver the way the legacy screen did.
     *
     * Generates CNumber / CNumberDisplay for a new driver, fills the defaults
     * the table requires (Active, AccountRefid, Modified_By), and decides
     * insert vs update from the id on the payload.
     */
    DriverMasterDto processDriver(DriverMasterDto dto, Integer companyId);

    /**
     * Search drivers with pagination and filtering
     * Equivalent to C# SelectDriver method
     *
     * @param companyId Company ID (required)
     * @param startIndex Zero-based offset. If -1, returns last page start offset
     * @param pageCount Number of records per page (if <= 0, return all records)
     * @param keyword Search keyword (optional)
     * @param column Column to search: "DriverName", "MobileNo", "Id", or "All"
     * @return DriverSearchResultDto containing list and total count
     */
    DriverSearchResultDto searchDrivers(Integer companyId, Integer startIndex, Integer pageCount, String keyword, String column);

    /**
     * Get all active drivers by company ID without pagination
     */
    List<DriverMasterDto> getAllDriverDetails(Integer companyId);
}
