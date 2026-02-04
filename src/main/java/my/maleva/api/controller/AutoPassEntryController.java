package my.maleva.api.controller;

import my.maleva.api.dto.AutoPassEntryDto;
import my.maleva.api.service.AutoPassEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/autopass-entries")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class AutoPassEntryController {

    private final AutoPassEntryService service;

    public AutoPassEntryController(AutoPassEntryService service) {
        this.service = service;
    }

    @GetMapping
    public List<AutoPassEntryDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public AutoPassEntryDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<AutoPassEntryDto> create(@Valid @RequestBody AutoPassEntryDto dto) {
        AutoPassEntryDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/autopass-entries/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public AutoPassEntryDto update(@PathVariable Integer id, @Valid @RequestBody AutoPassEntryDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
