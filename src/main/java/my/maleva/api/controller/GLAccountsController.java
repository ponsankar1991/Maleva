package my.maleva.api.controller;

import my.maleva.api.dto.GLAccountsDto;
import my.maleva.api.service.GLAccountsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gl-accounts")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class GLAccountsController {

    private final GLAccountsService service;

    public GLAccountsController(GLAccountsService service) {
        this.service = service;
    }

    @GetMapping
    public List<GLAccountsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public GLAccountsDto get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<GLAccountsDto> create(@Valid @RequestBody GLAccountsDto dto) {
        GLAccountsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/gl-accounts/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public GLAccountsDto update(@PathVariable UUID id, @Valid @RequestBody GLAccountsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
