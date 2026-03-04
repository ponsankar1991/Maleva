package my.maleva.api.controller;

import my.maleva.api.agentcompany.common.ApiResponse;
import my.maleva.api.dto.PortMasterDto;
import my.maleva.api.service.PortMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/port-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PortMasterController {

    private static final Logger logger = LoggerFactory.getLogger(PortMasterController.class);
    private final PortMasterService portMasterService;

    public PortMasterController(PortMasterService portMasterService) {
        this.portMasterService = portMasterService;
    }

    /**
     * Get all port records
     * GET /api/port-masters
     */
    @GetMapping
    public List<PortMasterDto> list() {
        return portMasterService.listAll();
    }

    /**
     * Get port record by ID
     * GET /api/port-masters/{id}
     */
    @GetMapping("/{id}")
    public PortMasterDto get(@PathVariable Integer id) {
        return portMasterService.getById(id);
    }

    /**
     * Create new port record
     * POST /api/port-masters
     */
    @PostMapping
    public ResponseEntity<PortMasterDto> create(@Valid @RequestBody PortMasterDto dto) {
        PortMasterDto saved = portMasterService.create(dto);
        return ResponseEntity.created(URI.create("/api/port-masters/" + saved.getId())).body(saved);
    }

    /**
     * Create multiple port records in batch
     * POST /api/port-masters/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<List<PortMasterDto>> createBatch(
            @NotNull @RequestParam Integer companyId,
            @Valid @RequestBody List<PortMasterDto> dtos) {
        List<PortMasterDto> saved = portMasterService.createBatch(companyId, dtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update port record
     * PUT /api/port-masters/{id}
     */
    @PutMapping("/{id}")
    public PortMasterDto update(@PathVariable Integer id, @Valid @RequestBody PortMasterDto dto) {
        return portMasterService.update(id, dto);
    }

    /**
     * Delete port record
     * DELETE /api/port-masters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        portMasterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all ports by company
     * GET /api/port-masters/company/{companyId}
     */
    @GetMapping("/company/{companyId}")
    public List<PortMasterDto> getByCompany(@PathVariable Integer companyId) {
        return portMasterService.getByCompany(companyId);
    }

    /**
     * Get active ports by company
     * GET /api/port-masters/company/{companyId}/active
     *
     * Returns all active ports (active=1) for the specified company
     *
     * @param companyId - Company reference ID (required, must be positive integer)
     * @return ResponseEntity with API response containing list of active ports
     *
     * Success Response (200 OK):
     * {
     *   "status": "SUCCESS",
     *   "statusCode": 200,
     *   "message": "Active ports retrieved successfully",
     *   "data": [
     *     {
     *       "id": 1,
     *       "companyRefId": 1,
     *       "portName": "Port A",
     *       "active": 1,
     *       "createdDate": "2026-01-15T10:30:00",
     *       "modifiedDate": "2026-01-15T10:30:00",
     *       "modifiedBy": "SYSTEM"
     *     }
     *   ],
     *   "count": 1,
     *   "timestamp": "2026-03-04T14:30:00"
     * }
     *
     * Error Response (400 Bad Request):
     * {
     *   "status": "ERROR",
     *   "statusCode": 400,
     *   "message": "Invalid company ID",
     *   "error": "Company ID must be a positive integer"
     * }
     */
    @GetMapping("/company/{companyId}/active")
    public ResponseEntity<ApiResponse<List<PortMasterDto>>> getActiveByCompany(
            @PathVariable Integer companyId) {
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Company ID must be a positive integer");
        }

        List<PortMasterDto> ports = portMasterService.getActiveByCompany(companyId);

        return ResponseEntity.ok(
                ApiResponse.success("Active ports retrieved successfully", ports)
        );
    }

    /**
     * Search port by name
     * GET /api/port-masters/search?companyId=1&portName=test
     */
    @GetMapping("/search")
    public List<PortMasterDto> search(
            @NotNull @RequestParam Integer companyId,
            @RequestParam(required = false) String portName) {
        return portMasterService.search(companyId, portName);
    }

    /**
     * Soft delete (set active to 2)
     * DELETE /api/port-masters/{id}/soft
     */
    @DeleteMapping("/{id}/soft")
    public ResponseEntity<Void> softDelete(@PathVariable Integer id) {
        portMasterService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activate port record
     * POST /api/port-masters/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public PortMasterDto activate(@PathVariable Integer id) {
        return portMasterService.activate(id);
    }

    /**
     * Deactivate port record
     * POST /api/port-masters/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public PortMasterDto deactivate(@PathVariable Integer id) {
        return portMasterService.deactivate(id);
    }
}

