package my.maleva.api.module.agent.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.common.dto.PagedResponse;
import my.maleva.api.module.agent.dto.AgentMasterCreateRequest;
import my.maleva.api.module.agent.dto.AgentMasterDto;
import my.maleva.api.module.agent.dto.AgentMasterUpdateRequest;
import my.maleva.api.module.agent.service.AgentMasterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/agent-masters")
@RequiredArgsConstructor
public class AgentMasterController {

    private final AgentMasterService service;

    @PermitAll
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AgentMasterDto>>> getAllAgents(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer companyRefId,
            @RequestParam(required = false) Boolean active) {
            
        log.debug("REST request to get a page of Agents");
        Page<AgentMasterDto> page = service.getAllAgents(pageable, search, companyRefId, active);
        PagedResponse<AgentMasterDto> response = new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Agents fetched successfully"));
    }

    @PermitAll
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentMasterDto>> getAgent(@PathVariable Integer id) {
        log.debug("REST request to get Agent : {}", id);
        AgentMasterDto dto = service.getAgentById(id);
        return ResponseEntity.ok(ApiResponse.success(dto, "Agent fetched successfully"));
    }

    @PermitAll
    @PostMapping
    public ResponseEntity<ApiResponse<AgentMasterDto>> createAgent(
            @Valid @RequestBody AgentMasterCreateRequest request) {
        log.debug("REST request to save Agent : {}", request.agentName());
        AgentMasterDto dto = service.createAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Agent created successfully"));
    }

    @PermitAll
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgentMasterDto>> updateAgent(
            @PathVariable Integer id,
            @Valid @RequestBody AgentMasterUpdateRequest request) {
        log.debug("REST request to update Agent : {}", id);
        AgentMasterDto dto = service.updateAgent(id, request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Agent updated successfully"));
    }

    @PermitAll
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAgent(@PathVariable Integer id) {
        log.debug("REST request to soft delete Agent : {}", id);
        service.softDeleteAgent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Agent soft deleted successfully"));
    }
}
