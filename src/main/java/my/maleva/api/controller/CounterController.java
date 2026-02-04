package my.maleva.api.controller;

import my.maleva.api.dto.CounterDto;
import my.maleva.api.service.CounterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/counters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CounterController {

    private final CounterService service;

    public CounterController(CounterService service) {
        this.service = service;
    }

    @GetMapping
    public List<CounterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CounterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CounterDto> create(@Valid @RequestBody CounterDto dto) {
        CounterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/counters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CounterDto update(@PathVariable Integer id, @Valid @RequestBody CounterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
