package my.maleva.api.agentcompany.controller;

import my.maleva.api.agentcompany.dto.AgentCompanyMasterDTO;
import my.maleva.api.agentcompany.entity.AgentCompanyMaster;
import my.maleva.api.agentcompany.mapper.AgentCompanyMasterMapper;
import my.maleva.api.agentcompany.repository.AgentCompanyMasterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agent-company")
public class AgentCompanyMasterController {

    private final AgentCompanyMasterRepository repository;
    private final AgentCompanyMasterMapper mapper;

    public AgentCompanyMasterController(AgentCompanyMasterRepository repository,
                                        AgentCompanyMasterMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @GetMapping
    public List<AgentCompanyMasterDTO> listAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentCompanyMasterDTO> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AgentCompanyMasterDTO> create(@RequestBody AgentCompanyMasterDTO dto) {
        AgentCompanyMaster entity = mapper.toEntity(dto);
        AgentCompanyMaster saved = repository.save(entity);
        AgentCompanyMasterDTO resultDto = mapper.toDto(saved);
        return ResponseEntity.created(URI.create("/api/agent-company/" + saved.getId()))
                .body(resultDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgentCompanyMasterDTO> update(@PathVariable Long id, @RequestBody AgentCompanyMasterDTO dto) {
        return repository.findById(id).map(existing -> {
            mapper.updateEntityFromDto(dto, existing);
            existing.setId(id);
            AgentCompanyMaster updated = repository.save(existing);
            return ResponseEntity.ok(mapper.toDto(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return repository.findById(id).map(existing -> {
            repository.delete(existing);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
