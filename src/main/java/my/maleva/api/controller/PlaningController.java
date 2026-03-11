package my.maleva.api.controller;

import my.maleva.api.dto.PlaningNumberResponseDTO;
import my.maleva.api.service.SequenceNoMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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

    public PlaningController(SequenceNoMasterService sequenceNoMasterService) {
        this.sequenceNoMasterService = sequenceNoMasterService;
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
}


