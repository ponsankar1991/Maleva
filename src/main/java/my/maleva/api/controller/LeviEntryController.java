package my.maleva.api.controller;

import my.maleva.api.dto.LeviEntryDto;
import my.maleva.api.service.LeviEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/levi-entries")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class LeviEntryController {

    private final LeviEntryService service;

    public LeviEntryController(LeviEntryService service) {
        this.service = service;
    }

    @GetMapping
    public List<LeviEntryDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public LeviEntryDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<LeviEntryDto> create(@Valid @RequestBody LeviEntryDto dto) {
        LeviEntryDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/levi-entries/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public LeviEntryDto update(@PathVariable Integer id, @Valid @RequestBody LeviEntryDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
