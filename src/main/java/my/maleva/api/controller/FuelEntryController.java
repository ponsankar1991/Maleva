package my.maleva.api.controller;

import my.maleva.api.dto.FuelEntryDto;
import my.maleva.api.service.FuelEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/fuel-entries")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class FuelEntryController {

    private final FuelEntryService service;

    public FuelEntryController(FuelEntryService service) {
        this.service = service;
    }

    @GetMapping
    public List<FuelEntryDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public FuelEntryDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<FuelEntryDto> create(@Valid @RequestBody FuelEntryDto dto) {
        FuelEntryDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/fuel-entries/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public FuelEntryDto update(@PathVariable Integer id, @Valid @RequestBody FuelEntryDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
