package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.fleet.dto.DriverMasterDto;
import my.maleva.api.module.fleet.dto.DriverSearchResultDto;
import my.maleva.api.module.fleet.service.DriverMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

/**
 * DriverMasterController - REST Controller for DriverMaster API
 * Provides CRUD operations and search functionality with pagination
 */
@RestController
@RequestMapping("/api/driver-masters")
@Validated
@PermitAll
public class DriverMasterController {

    private static final Logger logger = LoggerFactory.getLogger(DriverMasterController.class);

    private final DriverMasterService service;

    public DriverMasterController(DriverMasterService service) {
        this.service = service;
    }

    /**
     * Get all drivers
     * GET /api/driver-masters
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DriverMasterDto>>> list() {
        logger.info("Fetching all drivers");
        return ResponseEntity.ok(ApiResponse.success(service.listAll(), "Success"));
    }

    /**
     * Get driver by ID
     * GET /api/driver-masters/{id}
     */
    @GetMapping("/{id}")
    public DriverMasterDto get(@PathVariable Integer id) {
        logger.info("Fetching driver by ID: {}", id);
        return service.getById(id);
    }

    /**
     * Create new driver
     * POST /api/driver-masters
     */
    @PostMapping
    public ResponseEntity<DriverMasterDto> create(@Valid @RequestBody DriverMasterDto dto) {
        logger.info("Creating new driver");
        DriverMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/driver-masters/" + saved.getId())).body(saved);
    }

    /**
     * Update driver
     * PUT /api/driver-masters/{id}
     */
    @PutMapping("/{id}")
    public DriverMasterDto update(@PathVariable Integer id, @Valid @RequestBody DriverMasterDto dto) {
        logger.info("Updating driver with ID: {}", id);
        return service.update(id, dto);
    }

    /**
     * Delete driver
     * DELETE /api/driver-masters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        logger.info("Deleting driver with ID: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search drivers with pagination and filtering
     * GET /api/driver-masters/search?companyId=&startIndex=&pageCount=&keyword=&column=
     *
     * Equivalent to C# SelectDriver method
     *
     * @param companyId Company ID (required)
     * @param startIndex Zero-based offset (default: 0). If -1, returns last page
     * @param pageCount Number of records per page (default: 0 = all records)
     * @param keyword Search keyword (optional)
     * @param column Column to search: "DriverName", "MobileNo", "Id", or "All" (default: "All")
     * @return ApiResponse<DriverSearchResultDto> with items and totalCount
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<DriverSearchResultDto>> searchDrivers(
            @RequestParam Integer companyId,
            @RequestParam(required = false, defaultValue = "0") Integer startIndex,
            @RequestParam(required = false, defaultValue = "0") Integer pageCount,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "All") String column) {

        logger.info("Searching drivers - company:{} startIndex:{} pageCount:{} keyword:{} column:{}",
                companyId, startIndex, pageCount, keyword, column);

        try {
            DriverSearchResultDto result = service.searchDrivers(companyId, startIndex, pageCount, keyword, column);
            logger.info("Search completed - found {} drivers", result.getItems().size());
            return ResponseEntity.ok(ApiResponse.success(result, "Search completed successfully"));
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid search parameters", ex);
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid search parameters: " + ex.getMessage(), 400));
        } catch (Exception ex) {
            logger.error("Error searching drivers", ex);
            return ResponseEntity.status(500).body(ApiResponse.error("Internal server error: " + ex.getMessage(), 500));
        }
    }

    @GetMapping("/selectalldriverDetails")
    public ResponseEntity<ApiResponse<List<DriverMasterDto>>> selectalldriverDetails(@RequestParam Integer companyId) {
        logger.info("Getting all driver details for companyId:{}", companyId);
        return ResponseEntity.ok(ApiResponse.success(service.getAllDriverDetails(companyId), "Success"));
    }
}
