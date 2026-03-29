package my.maleva.api.module.agent.controller;

import my.maleva.api.module.company.dto.AgentDto;
import my.maleva.api.module.company.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class AgentController {

    private final AgentService service;

    public AgentController(AgentService service) {
        this.service = service;
    }

    @GetMapping
    public List<AgentDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public AgentDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<AgentDto> create(@Valid @RequestBody AgentDto dto) {
        AgentDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/agents/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public AgentDto update(@PathVariable Integer id, @Valid @RequestBody AgentDto dto) {
        return service.update(id, dto);
    }

    /**
     * Select all agents for a specific company with optional filtering.
     * Equivalent to the .NET SelectAgentAll method.
     *
     * Endpoint: POST /api/agents/select-all?companyRefId=X&jobId=Y
     *
     * @param companyRefId The company reference ID (required, must be > 0)
     * @param jobId The agent company reference ID for filtering (optional, 0 or not provided means no filter)
     * @return List of agents filtered by Active != 2, sorted by agentName
     *
     * Response:
     * {
     *   "ok": true,
     *   "message": "Agents retrieved successfully",
     *   "data": [ {...}, {...} ],
     *   "count": 2
     * }
     */
    @PostMapping("/select-all")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<AgentSelectionResponse> selectAgentAll(
            @RequestParam(value = "companyRefId") Integer companyRefId,
            @RequestParam(value = "jobId", defaultValue = "0") Integer jobId) {
        try {
            // Validate companyRefId
            if (companyRefId == null || companyRefId <= 0) {
                return ResponseEntity.badRequest()
                        .body(AgentSelectionResponse.failure("CompanyRefId must be a valid positive integer"));
            }

            // Call service to get agents
            List<AgentDto> agents = service.selectAgentAll(companyRefId, jobId);

            // Return success response
            AgentSelectionResponse response = AgentSelectionResponse.success(
                    "Agents retrieved successfully",
                    agents,
                    agents.size()
            );
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body(AgentSelectionResponse.failure(
                            ex.getMessage() != null ? ex.getMessage() : "An error occurred while retrieving agents"
                    ));
        }
    }

    /**
     * Response wrapper class for selectAgentAll endpoint.
     * Mirrors the .NET API response structure.
     */
    public static class AgentSelectionResponse {
        public Boolean ok;
        public String message;
        public List<AgentDto> data;
        public Integer count;
        public String error;

        private AgentSelectionResponse() {
        }

        public static AgentSelectionResponse success(String message, List<AgentDto> data, Integer count) {
            AgentSelectionResponse response = new AgentSelectionResponse();
            response.ok = true;
            response.message = message;
            response.data = data;
            response.count = count;
            response.error = null;
            return response;
        }

        public static AgentSelectionResponse failure(String errorMessage) {
            AgentSelectionResponse response = new AgentSelectionResponse();
            response.ok = false;
            response.message = errorMessage;
            response.data = null;
            response.count = 0;
            response.error = errorMessage;
            return response;
        }

        // Getters and setters for JSON serialization
        public Boolean getOk() {
            return ok;
        }

        public void setOk(Boolean ok) {
            this.ok = ok;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<AgentDto> getData() {
            return data;
        }

        public void setData(List<AgentDto> data) {
            this.data = data;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
