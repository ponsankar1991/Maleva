package my.maleva.api.controller;

import my.maleva.api.dto.PlaningNumberResponseDTO;
import my.maleva.api.service.SequenceNoMasterService;
import my.maleva.api.dto.PlanningF5View;
import my.maleva.api.dto.request.PlanningF5RequestDto;
import my.maleva.api.dto.request.PLANINGSearchRequestDto;
import my.maleva.api.dto.PlanningDetailsModel;
import my.maleva.api.service.PlanningMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * REST Controller for PLANNING Sequence Generation
 *
 * Single production-ready endpoint to get the next PLANNING sequence number.
 * Equivalent to .NET: public ResponseViewModel MaxPLANINGNo(int Comid, string BillType)
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

    private final SequenceNoMasterService sequenceNoMasterService;
    private final PlanningMasterService planningMasterService;

    public PlaningController(SequenceNoMasterService sequenceNoMasterService,
                            PlanningMasterService planningMasterService) {
        this.sequenceNoMasterService = sequenceNoMasterService;
        this.planningMasterService = planningMasterService;
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
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
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
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<PlanningF5View> selectPlanning(@RequestBody @Valid PlanningF5RequestDto filter) {
        PlanningF5View result = planningMasterService.selectPlanning(filter);
        return ResponseEntity.ok(result);
    }
    @PostMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<List<PlanningDetailsModel>> planningSearch(
            @RequestBody @Valid PLANINGSearchRequestDto filter) {
        List<PlanningDetailsModel> result = planningMasterService.planningSearch(filter);
        return ResponseEntity.ok(result);
    }

}




