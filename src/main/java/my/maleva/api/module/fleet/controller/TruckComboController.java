package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.constant.SecurityConstants;
import my.maleva.api.common.dto.ComboListModel;
import my.maleva.api.module.fleet.dto.request.TruckComboRequest;
import my.maleva.api.common.dto.ResponseViewModel;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.fleet.service.TruckMasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

/**
 * TruckComboController - Production-Grade REST Controller for Truck Combo/Dropdown API
 * 
 * Equivalent to .NET GetTruck service method
 * Provides optimized truck data for UI dropdowns and combo boxes
 * 
 * Architecture Highlights:
 * - Constructor injection for dependency management (immutability)
 * - Global exception handling (no try-catch blocks)
 * - Input validation via @Valid annotation
 * - Consistent logging with @Slf4j
 * - RESTful design principles
 * - DRY principle (no code duplication)
 * 
 * Endpoints:
 * - GET  /api/truck-combo?companyId=1
 * - GET  /api/truck-combo?companyId=1&type=40FT
 * - POST /api/truck-combo
 * 
 * @author System
 * @version 2.0
 * @since March 2026
 */
@Slf4j
@RestController
@RequestMapping("/api/truck-combo")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TruckComboController {

    // ==================== Constants ====================
    private static final String LOG_REQUEST_GET = "GET /api/truck-combo - companyId={}, type={}";
    private static final String LOG_REQUEST_POST = "POST /api/truck-combo - companyId={}, type={}";
    private static final String LOG_SUCCESS = "Successfully fetched {} truck(s) for companyId={}";
    private static final String LOG_VALIDATION_ERROR = "Validation error: {}";
    
    private static final String ERR_COMPANY_ID_INVALID = "Company ID is required and must be a positive integer";
    private static final String MSG_SUCCESS = "Success";

    // ==================== Dependencies ====================
    private final TruckMasterService truckMasterService;

    /**
     * Constructor Injection
     * 
     * Benefits:
     * - Immutability (final field)
     * - Explicit dependencies
     * - Easy unit testing
     * - No NullPointerException risks
     * 
     * @param truckMasterService Service for truck operations
     */
    public TruckComboController(TruckMasterService truckMasterService) {
        this.truckMasterService = truckMasterService;
    }

    /**
     * GET /api/truck-combo
     * Retrieve truck combo list with query parameters
     * 
     * Equivalent to .NET GetTruck(int companyId, string type)
     * 
     * Query Parameters:
     * - companyId (required): Company ID (must be positive)
     * - type (optional): Truck type filter (e.g., "40FT", "20FT")
     * 
     * Example Requests:
     * GET /api/truck-combo?companyId=1
     * GET /api/truck-combo?companyId=1&type=40FT
     * 
     * Success Response (HTTP 200):
     * {
     *   "isSuccess": true,
     *   "statusCode": 200,
     *   "message": "Success",
     *   "data1": [
     *     { "id": 1, "accountName": "TRUCK-001" },
     *     { "id": 2, "accountName": "TRUCK-002" }
     *   ]
     * }
     * 
     * @param companyId Required: Company ID (must be positive)
     * @param type Optional: Truck type filter
     * @return ResponseEntity with ResponseViewModel
     * @throws InvalidRequestException if companyId is null or invalid
     */
    @GetMapping
    @PermitAll
    public ResponseEntity<ResponseViewModel> getTruckCombo(
            @RequestParam Integer companyId,
            @RequestParam(name = "type", required = false) String type) {

        log.info(LOG_REQUEST_GET, companyId, type);

        // Validation (throws InvalidRequestException which is handled by GlobalExceptionHandler)
        validateCompanyId(companyId);

        // Fetch truck combo data from service
        List<ComboListModel> trucks = truckMasterService.getTruckCombo(companyId, type);

        // Logging
        log.debug(LOG_SUCCESS, trucks.size(), companyId);

        // Build and return success response using builder pattern
        return ResponseEntity.ok(
            ResponseViewModel.builder()
                .isSuccess(true)
                .statusCode(HttpStatus.OK.value())
                .message(MSG_SUCCESS)
                .data1(trucks)
                .build()
        );
    }

    /**
     * POST /api/truck-combo
     * Alternative endpoint for truck combo retrieval
     * 
     * Useful for:
     * - Request body logging and auditing
     * - Future extensibility (pagination, sorting)
     * - Complex filtering scenarios
     * 
     * Request Body:
     * {
     *   "companyId": 1,
     *   "type": "40FT"
     * }
     * 
     * @param request TruckComboRequest with companyId and optional type
     * @return ResponseEntity with ResponseViewModel
     * @throws InvalidRequestException if validation fails
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<ResponseViewModel> getTruckComboPost(
            @Valid @RequestBody TruckComboRequest request) {

        log.info(LOG_REQUEST_POST, request.getCompanyId(), request.getType());

        // Validation (redundant but safe; primary validation via @Valid annotation)
        validateCompanyId(request.getCompanyId());

        // Fetch truck combo data from service
        List<ComboListModel> trucks = truckMasterService.getTruckCombo(
            request.getCompanyId(),
            request.getType()
        );

        // Logging
        log.debug(LOG_SUCCESS, trucks.size(), request.getCompanyId());

        // Build and return success response
        return ResponseEntity.ok(
            ResponseViewModel.builder()
                .isSuccess(true)
                .statusCode(HttpStatus.OK.value())
                .message(MSG_SUCCESS)
                .data1(trucks)
                .build()
        );
    }

    /**
     * Validate company ID
     * 
     * Extracted to prevent code duplication
     * Used in both GET and POST endpoints
     * 
     * @param companyId Company ID to validate
     * @throws InvalidRequestException if validation fails
     */
    private void validateCompanyId(Integer companyId) {
        if (companyId == null || companyId <= 0) {
            log.warn(LOG_VALIDATION_ERROR, ERR_COMPANY_ID_INVALID);
            throw new InvalidRequestException(ERR_COMPANY_ID_INVALID);
        }
    }
}


