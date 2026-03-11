package my.maleva.api.controller;

import my.maleva.api.dto.PlaningNumberResponseDTO;
import my.maleva.api.service.SequenceNoMasterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for PLANNING-related operations
 * Handles PLANNING sequence number generation and management
 *
 * Conversion from .NET Framework using Dapper to Spring Boot with JPA
 * Legacy Method: MaxPLANINGNo(int Comid, string BillType)
 * New Endpoints:
 * - POST /api/planing/max-planning-no/{companyId}
 * - POST /api/planing/max-planning-no
 */
@RestController
@RequestMapping("/api/planing")
public class PlaningController {

    private final SequenceNoMasterService sequenceNoMasterService;

    public PlaningController(SequenceNoMasterService sequenceNoMasterService) {
        this.sequenceNoMasterService = sequenceNoMasterService;
    }

    /**
     * Get the next PLANNING sequence number (PathVariable version)
     *
     * Equivalent to the .NET method:
     * public ResponseViewModel MaxPLANINGNo(int Comid, string BillType)
     *
     * Endpoint: POST /api/planing/max-planning-no/{companyId}
     *
     * Response Format (on success):
     * {
     *   "isSuccess": true,
     *   "statusCode": 1,
     *   "message": "Success",
     *   "data1": "PL000000001",
     *   "companyId": 1
     * }
     *
     * Response Format (on error):
     * {
     *   "isSuccess": false,
     *   "statusCode": 0,
     *   "message": "Error message",
     *   "data1": "Api Details : PLANING_MaxPLANINGNo",
     *   "companyId": 1
     * }
     *
     * @param companyId the company reference ID
     * @return ResponseEntity containing the next PLANNING sequence number
     */
    @PostMapping("/max-planning-no/{companyId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<Map<String, Object>> getMaxPlanningNoPathVariable(
            @PathVariable Integer companyId) {
        return getMaxPlanningNoInternal(companyId);
    }

    /**
     * Get the next PLANNING sequence number (RequestParam version)
     *
     * Equivalent to the .NET method:
     * public ResponseViewModel MaxPLANINGNo(int Comid, string BillType)
     *
     * Endpoint: POST /api/planing/max-planning-no?companyId=1
     *
     * Response Format (on success):
     * {
     *   "isSuccess": true,
     *   "statusCode": 1,
     *   "message": "Success",
     *   "data1": "PL000000001",
     *   "companyId": 1
     * }
     *
     * Response Format (on error):
     * {
     *   "isSuccess": false,
     *   "statusCode": 0,
     *   "message": "Error message",
     *   "data1": "Api Details : PLANING_MaxPLANINGNo",
     *   "companyId": 1
     * }
     *
     * @param companyId the company reference ID (query parameter)
     * @return ResponseEntity containing the next PLANNING sequence number
     */
    @PostMapping("/max-planning-no")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<Map<String, Object>> getMaxPlanningNoQueryParam(
            @RequestParam(name = "companyId") Integer companyId) {
        return getMaxPlanningNoInternal(companyId);
    }

    /**
     * Internal method to handle PLANNING sequence number generation
     *
     * This method replicates the logic from the .NET Dapper version:
     *
     * .NET Code:
     * ro.Data1 = "PL" + (_dapper.ExecuteScalar("(SELECT ISNULL(MAX(SequenceNo)+1,1) As RefNo
     *            FROM SequenceNoMaster WITH (NOLOCK) Where CompanyRefId = " + Comid +
     *            " and SequenceName='PLANINGMaster')") ?? 0).ToString().PadLeft(9, '0');
     *
     * Spring JPA Equivalent:
     * 1. Query SequenceNoMaster table for sequenceName='PLANINGMaster' and companyRefId=companyId
     * 2. Get MAX(sequenceNo) and add 1 (or return 1 if null/0)
     * 3. Format as "PL" + 9-digit padded number
     * 4. Save/update the sequence record
     * 5. Return formatted response
     *
     * @param companyId the company reference ID
     * @return ResponseEntity with formatted response matching .NET ResponseViewModel structure
     */
    private ResponseEntity<Map<String, Object>> getMaxPlanningNoInternal(Integer companyId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Generate the next PLANNING sequence number
            String planningSequenceNo = sequenceNoMasterService.getMaxPlanningNo(companyId);

            // Build success response matching .NET ResponseViewModel structure
            response.put("isSuccess", true);
            response.put("statusCode", 1);  // EnumManager.Status.Success
            response.put("message", "Success");
            response.put("data1", planningSequenceNo);  // PL000000001, PL000000002, etc.
            response.put("companyId", companyId);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            // Log the error for debugging
            // _logErrors.Writelog(ex, "PLANING", "MaxPLANINGNo");  // Equivalent to .NET error logging

            // Build error response matching .NET ResponseViewModel structure
            response.put("isSuccess", false);
            response.put("statusCode", 0);  // EnumManager.Status.Error
            response.put("data1", "Api Details : PLANING_MaxPLANINGNo");  // Error context
            response.put("message", ex.getLocalizedMessage() != null ?
                         ex.getLocalizedMessage() : ex.getMessage());
            response.put("companyId", companyId);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Alternative endpoint that returns a DTO-based response
     * Uses MapStruct mapper for entity-DTO conversion
     *
     * Endpoint: POST /api/planing/generate-sequence/{companyId}
     *
     * @param companyId the company reference ID
     * @return ResponseEntity containing PlaningNumberResponseDTO
     */
    @PostMapping("/generate-sequence/{companyId}")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<PlaningNumberResponseDTO> generatePlanningSequence(
            @PathVariable Integer companyId) {
        try {
            String sequenceNo = sequenceNoMasterService.getMaxPlanningNo(companyId);

            PlaningNumberResponseDTO dto = PlaningNumberResponseDTO.builder()
                    .sequenceNumber(sequenceNo)
                    .companyId(companyId)
                    .success(true)
                    .build();

            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            PlaningNumberResponseDTO errorDto = PlaningNumberResponseDTO.builder()
                    .success(false)
                    .error(ex.getMessage())
                    .companyId(companyId)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
        }
    }
}

