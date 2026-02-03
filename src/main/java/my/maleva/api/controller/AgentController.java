package my.maleva.api.controller;

import my.maleva.api.dto.AgentDto;
import my.maleva.api.service.AgentService;
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
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
