package my.maleva.api.controller;

import my.maleva.api.dto.MSICCodeDto;
import my.maleva.api.service.MSICCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/msic-codes")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class MSICCodeController {

    private final MSICCodeService service;

    public MSICCodeController(MSICCodeService service) {
        this.service = service;
    }

    @GetMapping
    public List<MSICCodeDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public MSICCodeDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<MSICCodeDto> create(@Valid @RequestBody MSICCodeDto dto) {
        MSICCodeDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/msic-codes/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public MSICCodeDto update(@PathVariable Integer id, @Valid @RequestBody MSICCodeDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
