package my.maleva.api.controller;

import my.maleva.api.dto.ClassificationDto;
import my.maleva.api.service.ClassificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/classifications")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ClassificationController {

    private final ClassificationService service;

    public ClassificationController(ClassificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClassificationDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ClassificationDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ClassificationDto> create(@Valid @RequestBody ClassificationDto dto) {
        ClassificationDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/classifications/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ClassificationDto update(@PathVariable Integer id, @Valid @RequestBody ClassificationDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
