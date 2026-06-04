package my.maleva.api.module.master.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.master.dto.SequenceNoMasterDto;
import my.maleva.api.module.master.service.SequenceNoMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for SequenceNoMaster endpoints
 * Provides CRUD operations and sequence number generation
 */
@RestController
@RequestMapping("/api/sequence-masters")
@Validated
public class SequenceNoMasterController {

    private final SequenceNoMasterService service;

    public SequenceNoMasterController(SequenceNoMasterService service) {
        this.service = service;
    }

    /**
     * Get all sequence records
     * GET /api/sequence-masters
     *
     * @return list of all sequence DTOs
     */
    @GetMapping
    @PermitAll
    public List<SequenceNoMasterDto> list() {
        return service.listAll();
    }

    /**
     * Get sequence record by ID
     * GET /api/sequence-masters/{id}
     *
     * @param id the sequence ID
     * @return the sequence DTO
     */
    @GetMapping("/{id}")
    @PermitAll
    public SequenceNoMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    /**
     * Get all sequences for a company
     * GET /api/sequence-masters/company/{companyRefId}
     *
     * @param companyRefId the company ID
     * @return list of sequence DTOs for the company
     */
    @GetMapping("/company/{companyRefId}")
    @PermitAll
    public ResponseEntity<List<SequenceNoMasterDto>> getByCompanyId(@PathVariable Integer companyRefId) {
        List<SequenceNoMasterDto> sequences = service.getByCompanyId(companyRefId);
        return ResponseEntity.ok(sequences);
    }

    /**
     * Create a new sequence record
     * POST /api/sequence-masters
     *
     * @param dto the sequence DTO
     * @return created sequence DTO with Location header
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<SequenceNoMasterDto> create(@Valid @RequestBody SequenceNoMasterDto dto) {
        SequenceNoMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/sequence-masters/" + saved.getId())).body(saved);
    }

    /**
     * Update a sequence record
     * PUT /api/sequence-masters/{id}
     *
     * @param id the sequence ID
     * @param dto the updated sequence DTO
     * @return updated sequence DTO
     */
    @PutMapping("/{id}")
    @PermitAll
    public SequenceNoMasterDto update(@PathVariable Integer id, @Valid @RequestBody SequenceNoMasterDto dto) {
        return service.update(id, dto);
    }

    /**
     * Delete a sequence record
     * DELETE /api/sequence-masters/{id}
     *
     * @param id the sequence ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Generate the next sequence number for a bill type
     * Equivalent to the legacy MaxSaleOrderNo method
     * POST /api/sequence-masters/company/{companyId}/generate-next
     *
     * Request Body:
     * {
     *   "billType": "SO"
     * }
     *
     * @param companyId the company ID
     * @param request contains billType
     * @return formatted sequence number (e.g., "SO000000001")
     */

    @PermitAll
    public ResponseEntity<Map<String, String>> generateNextSequenceNo(
            @PathVariable Integer companyId,
            @Valid @RequestBody Map<String, String> request) {
        String billType = request.get("billType");
        if (billType == null || billType.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "billType is required"));
        }
        String nextSequenceNo = service.generateNextSequenceNo(companyId, billType);
        return ResponseEntity.ok(Map.of("sequenceNo", nextSequenceNo));
    }

    /**
     * Get all sequences for a company in a specific year
     * GET /api/sequence-masters/company/{companyRefId}/year/{year}
     *
     * @param companyRefId the company ID
     * @param year the year
     * @return list of sequence DTOs for the year
     */
    @GetMapping("/company/{companyRefId}/year/{year}")
    @PermitAll
    public ResponseEntity<List<SequenceNoMasterDto>> getSequencesByYear(
            @PathVariable Integer companyRefId,
            @PathVariable Integer year) {
        List<SequenceNoMasterDto> sequences = service.getSequencesByCompanyAndYear(companyRefId, year);
        return ResponseEntity.ok(sequences);
    }

    /**
     * Get the maximum sequence number for a bill type
     * This is the equivalent of the legacy MaxSaleOrderNo method for checking current number
     * GET /api/sequence-masters/company/{companyId}/max-sequence
     * Query Params: billType=SO
     *
     * @param companyId the company ID
     * @param billType the bill type (e.g., "SO", "INV", etc.)
     * @return formatted sequence number
     */
    @GetMapping("/company/{companyId}/max-sequence")
    @PermitAll
    public ResponseEntity<Map<String, Object>> getMaxSequenceNo(
            @PathVariable Integer companyId,
            @RequestParam String billType) {
        String maxSequenceNo = service.getMaxSaleOrderNo(companyId, billType);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "No", maxSequenceNo,
                "companyId", companyId,
                "billType", billType
        ));
    }

    /**
     * Get the next PLANNING sequence number
     * Equivalent to the legacy .NET MaxPLANINGNo method
     * POST /api/sequence-masters/company/{companyId}/max-planning-no
     *
     * This endpoint generates and returns the next PLANNING sequence number
     * with format: PL + 9-digit padded number (e.g., PL000000001)
     *
     * @param companyId the company ID
     * @return response containing the generated PLANNING sequence number
     */
    @PostMapping("/company/{companyId}/max-planning-no")
    @PermitAll
    public ResponseEntity<Map<String, Object>> getMaxPlanningNo(@PathVariable Integer companyId) {
        try {
            String planningSequenceNo = service.getMaxPlanningNo(companyId);
            return ResponseEntity.ok(Map.of(
                    "isSuccess", true,
                    "statusCode", 1,
                    "message", "Success",
                    "data1", planningSequenceNo,
                    "companyId", companyId
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "isSuccess", false,
                    "statusCode", 0,
                    "message", ex.getMessage(),
                    "data1", "Api Details : PLANING_MaxPLANINGNo",
                    "companyId", companyId
            ));
        }
    }
}



