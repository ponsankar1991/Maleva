package my.maleva.api.module.planning.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.planning.dto.PlaningNumberResponseDTO;
import my.maleva.api.module.planning.dto.PlanningEditResponseDto;
import my.maleva.api.module.planning.dto.PlanningRequest;
import my.maleva.api.module.planning.dto.PlanningSaveResponseDto;
import my.maleva.api.module.master.service.SequenceNoMasterService;
import my.maleva.api.module.planning.dto.PlanningF5View;
import my.maleva.api.module.planning.dto.request.PlanningF5RequestDto;
import my.maleva.api.module.planning.dto.request.PLANINGSearchRequestDto;
import my.maleva.api.module.planning.dto.PlanningDetailsModel;
import my.maleva.api.module.planning.service.PlanningMasterService;
import my.maleva.api.module.planning.service.PlanningSaveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * REST Controller for PLANNING operations.
 * Handles sequence generation, insert/update, delete, and search.
 *
 * Features:
 * - Input validation (@NotNull @Positive)
 * - Global exception handling (no try-catch)
 * - Clean, maintainable code
 * - Security authorization
 */
@RestController
@RequestMapping("/api/planing")
public class PlaningController {

    private static final Logger logger = LoggerFactory.getLogger(PlaningController.class);

    private final SequenceNoMasterService sequenceNoMasterService;
    private final PlanningMasterService planningMasterService;
    private final PlanningSaveService planningSaveService;

    public PlaningController(SequenceNoMasterService sequenceNoMasterService,
                            PlanningMasterService planningMasterService,
                            PlanningSaveService planningSaveService) {
        this.sequenceNoMasterService = sequenceNoMasterService;
        this.planningMasterService = planningMasterService;
        this.planningSaveService = planningSaveService;
    }

    /**
     * Get the next PLANNING sequence number
     *
     * Endpoint: POST /api/planing/max-planning-no/{companyId}
     *
     * Example:
     * POST http://localhost:8080/api/planing/max-planning-no/1
     * Authorization: Bearer YOUR_JWT_TOKEN
     *
     * Response (Success - HTTP 200):
     * {
     *   "sequenceNumber": "PL000000001",
     *   "companyId": 1,
     *   "success": true
     * }
     *
     * Response (Error - HTTP 400/404/500):
     * {
     *   "timestamp": "2026-03-11T10:30:00Z",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Company ID must be positive. Received: 0",
     *   "path": "/api/planing/max-planning-no/0"
     * }
     *
     * @param companyId the company ID (must be positive)
     * @return PlaningNumberResponseDTO with sequence number
     * @throws IllegalArgumentException if companyId is invalid (caught by GlobalExceptionHandler)
     * @throws RuntimeException if database operation fails (caught by GlobalExceptionHandler)
     */
    @PostMapping("/max-planning-no/{companyId}")
    @PermitAll
    public ResponseEntity<PlaningNumberResponseDTO> getMaxPlanningNo(
            @PathVariable @NotNull @Positive Integer companyId) {

        // Call service to get next PLANNING sequence number
        // Exceptions are handled by GlobalExceptionHandler
        String sequenceNumber = sequenceNoMasterService.getMaxPlanningNo(companyId);

        // Build success response
        PlaningNumberResponseDTO response = PlaningNumberResponseDTO.builder()
                .sequenceNumber(sequenceNumber)
                .companyId(companyId)
                .success(true)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * SelectPLANING - Complex filtered search equivalent to .NET SelectPLANING method
     * Returns combined PlanningMaster and PlanningDetails data with dynamic filtering
     *
     * Endpoint: POST /api/planing/select-planning
     *
     * Request Body:
     * {
     *   "comid": 1,
     *   "employeeid": 123,  // optional
     *   "search": "PL0001", // optional, overrides date filters
     *   "fromdate": "2024-01-01", // optional
     *   "todate": "2024-12-31"    // optional
     * }
     *
     * Response (Success - HTTP 200):
     * {
     *   "salemaster": [
     *     {
     *       "Id": 1,
     *       "PLANINGNo": 1,
     *       "PLANINGNoDisplay": "PL000000001",
     *       "PLANINGDate": "15/03/2024",
     *       "Remarks": "Sample remarks"
     *     }
     *   ],
     *   "saledetails": [
     *     {
     *       "Id": 1,
     *       "PLANINGMasterRefId": 1,
     *       "SaleOrderMasterRefId": 123,
     *       "TruckName": "Truck A",
     *       "Remarks": "Detail remarks",
     *       "JobNo": "SO000000123",
     *       "JobDate": "10/03/2024",
     *       "CustomerName": "ABC Corp"
     *     }
     *   ]
     * }
     *
     * @param filter the filter criteria
     * @return PlanningF5View with combined master and detail data
     */
    @PostMapping("/select-planning")
    @PermitAll
    public ResponseEntity<PlanningF5View> selectPlanning(@RequestBody @Valid PlanningF5RequestDto filter) {
        PlanningF5View result = planningMasterService.selectPlanning(filter);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/edit")
@PermitAll
    public ResponseEntity<PlanningEditResponseDto> editPlanning(
            @RequestParam(required = false) @Positive Integer id,
            @RequestParam(required = false) @Positive Integer planningNo,
            @RequestParam("companyId") @NotNull @Positive Integer companyId) {
        PlanningEditResponseDto result = planningMasterService.editPlanning(id, planningNo, companyId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/search")
    @PermitAll
    public ResponseEntity<List<PlanningDetailsModel>> planningSearch(
            @RequestBody @Valid PLANINGSearchRequestDto filter) {
        List<PlanningDetailsModel> result = planningMasterService.planningSearch(filter);
        return ResponseEntity.ok(result);
    }

    /**
     * Save (Insert or Update) Planning records.
     * Matches .NET: [HttpPost] InsertPLANING(List<PLANINGMasterModel> objBrand)
     *
     * Endpoint: POST /api/planing/save
     *
     * Frontend sends JSON array (List):
     * [
     *   {
     *     "id": 0,
     *     "sdid": 0,
     *     "companyRefId": 1,
     *     "employeeRefId": 123,
     *     "fDate": "2026/04/01",
     *     "tDate": "2026/04/30",
     *     "saleDate": "2026/04/05",
     *     "cNumberDisplay": "PL000000001",
     *     "cNumber": 1,
     *     "remarks": "Optional remarks",
     *     "search": "PORT1,PORT2",
     *     "saleDetails": [
     *       {
     *         "saleOrderMasterRefId": 456,
     *         "sortBy": 1,
     *         "truckRefid": 789,
     *         "originD": "Port A",
     *         "destinationD": "Port B",
     *         "pickupDate": "2026/04/10 08:00",
     *         "deliveryDate": "2026/04/15 18:00"
     *       }
     *     ]
     *   }
     * ]
     *
     * Response (Success):
     * [{ "ok": true, "message": "Planning saved successfully", "name": "PL000000001", "id": 1 }]
     *
     * Response (Error):
     * [{ "ok": false, "message": "Error description" }]
     */
    @PostMapping("/save")
    @PermitAll
    public ResponseEntity<List<PlanningSaveResponseDto>> savePlanning(
            @RequestBody List<PlanningRequest> requests,
            @RequestHeader(value = "Comid") Integer comid) {

        logger.info("Received save planning request: {} records, comid={}", requests.size(), comid);

        List<PlanningSaveResponseDto> results = planningSaveService.saveAll(requests, comid);
        return ResponseEntity.ok(results);
    }

    /**
     * Delete (soft delete) a Planning record.
     *
     * Endpoint: DELETE /api/planing/{id}?companyId=1
     *
     * Response (Success):
     * { "ok": true, "message": "Planning deleted successfully", "id": 1 }
     *
     * Response (Error):
     * { "ok": false, "message": "Planning not found" }
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<PlanningSaveResponseDto> deletePlanning(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {

        PlanningSaveResponseDto result = planningSaveService.delete(id, companyId);
        return ResponseEntity.ok(result);
    }
}




