package my.maleva.api.controller;

import my.maleva.api.dto.EngineHoursDto;
import my.maleva.api.service.EngineHoursService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/engine-hours")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class EngineHoursController {

    private final EngineHoursService service;

    public EngineHoursController(EngineHoursService service) {
        this.service = service;
    }

    @GetMapping
    public List<EngineHoursDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public EngineHoursDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<EngineHoursDto> create(@Valid @RequestBody EngineHoursDto dto) {
        EngineHoursDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/engine-hours/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public EngineHoursDto update(@PathVariable Integer id, @Valid @RequestBody EngineHoursDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
