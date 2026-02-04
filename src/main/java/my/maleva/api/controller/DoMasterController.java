package my.maleva.api.controller;

import my.maleva.api.dto.DoMasterDto;
import my.maleva.api.service.DoMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/do-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class DoMasterController {

    private final DoMasterService service;

    public DoMasterController(DoMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<DoMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public DoMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<DoMasterDto> create(@Valid @RequestBody DoMasterDto dto) {
        DoMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/do-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public DoMasterDto update(@PathVariable Integer id, @Valid @RequestBody DoMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
