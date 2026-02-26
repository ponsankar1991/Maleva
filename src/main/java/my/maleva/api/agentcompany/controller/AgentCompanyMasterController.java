package my.maleva.api.agentcompany.controller;

import my.maleva.api.agentcompany.dto.AgentCompanyMasterDTO;
import my.maleva.api.agentcompany.dto.AgentCompanyRequestDTO;
import my.maleva.api.agentcompany.service.AgentCompanyMasterService;
import my.maleva.api.agentcompany.service.AgentCompanyMasterService.EntityNotFoundException;
import my.maleva.api.agentcompany.service.AgentCompanyMasterService.InvalidRequestException;
import my.maleva.api.agentcompany.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for AgentCompanyMaster management.
 * Provides CRUD operations and special operations like bulk upsert via SP_AgentCompany logic.
 *
 * Base URL: /api/agent-companies
 *
 * Endpoints:
 * - GET    /api/agent-companies               → List all active agent companies
 * - POST   /api/agent-companies               → Create new agent company
 * - GET    /api/agent-companies/{id}          → Get agent company by ID
 * - PUT    /api/agent-companies/{id}          → Update agent company
 * - DELETE /api/agent-companies/{id}          → Delete (soft delete) agent company
 * - GET    /api/agent-companies/company/{companyRefId}  → Get by CompanyRefId
 * - POST   /api/agent-companies/upsert        → Bulk upsert (SP_AgentCompany logic)
 * - POST   /api/agent-companies/search        → Search agent companies
 */
@RestController
@RequestMapping("/api/agent-companies")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AgentCompanyMasterController {

    private final AgentCompanyMasterService service;

    public AgentCompanyMasterController(AgentCompanyMasterService service) {
        this.service = service;
    }

    /**
     * List all active agent companies (Active != 2).
     * GET /api/agent-companies
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ApiResponse<List<AgentCompanyMasterDTO>>> listAll() {
        try {
            List<AgentCompanyMasterDTO> data = service.getAllAgentCompanies();
            if (data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ApiResponse.failure(HttpStatus.NO_CONTENT, "No agent companies found"));
            }
            return ResponseEntity.ok(ApiResponse.success(
                    "Agent companies retrieved successfully",
                    data
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }
    }

    /**
     * Get agent company by ID.
     * GET /api/agent-companies/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ApiResponse<AgentCompanyMasterDTO>> getById(@PathVariable Long id) {
        try {
            AgentCompanyMasterDTO data = service.getAgentCompanyById(id);
            return ResponseEntity.ok(ApiResponse.success(
                    "Agent company retrieved successfully",
                    data
            ));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(HttpStatus.NOT_FOUND, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }
    }

    /**
     * Get all agent companies for a specific company reference ID.
     * GET /api/agent-companies/company/{companyRefId}
     * @param companyRefId The company reference ID
     */
    @GetMapping("/company/{companyRefId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ApiResponse<List<AgentCompanyMasterDTO>>> getByCompanyRefId(
            @PathVariable Integer companyRefId) {
        try {
            if (companyRefId == null || companyRefId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(HttpStatus.BAD_REQUEST,
                                "CompanyRefId must be a valid positive integer"));
            }

            List<AgentCompanyMasterDTO> data = service.getAgentCompaniesByCompanyRefId(companyRefId);
            if (data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ApiResponse.failure(HttpStatus.NO_CONTENT,
                                "No agent companies found for company: " + companyRefId));
            }
            return ResponseEntity.ok(ApiResponse.success(
                    "Agent companies for company retrieved successfully",
                    data
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }
    }

    /**
     * Create a new agent company.
     * POST /api/agent-companies
     * @param requestDto The request DTO with agent company data
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ApiResponse<AgentCompanyMasterDTO>> create(
            @RequestBody AgentCompanyRequestDTO requestDto) {
        try {
            // Convert RequestDTO to DTO
            AgentCompanyMasterDTO dto = new AgentCompanyMasterDTO();
            dto.setCompanyRefId(requestDto.getCompanyRefId());
            dto.setName(requestDto.getName());
            dto.setDFlag(requestDto.getDFlag());
            dto.setActive(requestDto.getActive());

            AgentCompanyMasterDTO created = service.createAgentCompany(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Agent company created successfully",
                            created
                    ));
        } catch (InvalidRequestException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }
    }

    /**
     * Update an existing agent company.
     * PUT /api/agent-companies/{id}
     * @param id The agent company ID
     * @param requestDto The request DTO with updated data
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ApiResponse<AgentCompanyMasterDTO>> update(
            @PathVariable Long id,
            @RequestBody AgentCompanyRequestDTO requestDto) {
        try {
            // Convert RequestDTO to DTO
            AgentCompanyMasterDTO dto = new AgentCompanyMasterDTO();
            dto.setId(id);
            dto.setCompanyRefId(requestDto.getCompanyRefId());
            dto.setName(requestDto.getName());
            dto.setDFlag(requestDto.getDFlag());
            dto.setActive(requestDto.getActive());

            AgentCompanyMasterDTO updated = service.updateAgentCompany(id, dto);
            return ResponseEntity.ok(ApiResponse.success(
                    "Agent company updated successfully",
                    updated
            ));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(HttpStatus.NOT_FOUND, ex.getMessage()));
        } catch (InvalidRequestException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }
    }

    /**
     * Delete (soft delete) an agent company.
     * DELETE /api/agent-companies/{id}
     * @param id The agent company ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            service.deleteAgentCompany(id);
            return ResponseEntity.noContent()
                    .build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(HttpStatus.NOT_FOUND, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }
    }

    /**
     * Bulk upsert agent companies (implements SP_AgentCompany logic).
     * For each record: if exists with CompanyRefId + Name + Active=1, update; otherwise insert.
     *
     * POST /api/agent-companies/upsert?companyRefId=1
     * Body: [{ "name": "Company1", "dFlag": 0, "active": 1 }, ...]
     *
     * @param companyRefId The company reference ID (from frontend/query parameter)
     * @param dtos List of agent companies to upsert
     */
    @PostMapping("/upsert")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ApiResponse<List<AgentCompanyMasterDTO>>> upsert(
            @RequestParam("companyRefId") Integer companyRefId,
            @RequestBody List<AgentCompanyRequestDTO> dtos) {
        try {
            if (companyRefId == null || companyRefId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(HttpStatus.BAD_REQUEST,
                                "CompanyRefId must be provided and must be a valid positive integer"));
            }

            if (dtos == null || dtos.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(HttpStatus.BAD_REQUEST,
                                "Agent companies list cannot be empty"));
            }

            // Convert RequestDTOs to DTOs
            List<AgentCompanyMasterDTO> masterDtos = dtos.stream()
                    .map(requestDto -> {
                        AgentCompanyMasterDTO dto = new AgentCompanyMasterDTO();
                        dto.setCompanyRefId(companyRefId);
                        dto.setName(requestDto.getName());
                        dto.setDFlag(requestDto.getDFlag());
                        dto.setActive(requestDto.getActive());
                        return dto;
                    })
                    .toList();

            List<AgentCompanyMasterDTO> upserted = service.upsertAgentCompanies(companyRefId, masterDtos);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            "Agent companies upserted successfully",
                            upserted
                    ));
        } catch (InvalidRequestException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }
    }

    /**
     * Search agent companies by company reference ID.
     * POST /api/agent-companies/search?companyRefId=1
     * @param companyRefId The company reference ID
     */
    @PostMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
    public ResponseEntity<ApiResponse<List<AgentCompanyMasterDTO>>> search(
            @RequestParam("companyRefId") Integer companyRefId) {
        try {
            if (companyRefId == null || companyRefId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(HttpStatus.BAD_REQUEST,
                                "CompanyRefId must be a valid positive integer"));
            }

            List<AgentCompanyMasterDTO> data = service.searchByCompanyRefId(companyRefId);
            if (data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ApiResponse.failure(HttpStatus.NO_CONTENT,
                                "No agent companies found for search criteria"));
            }
            return ResponseEntity.ok(ApiResponse.success(
                    "Agent companies search completed successfully",
                    data
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()));
        }
    }
}
