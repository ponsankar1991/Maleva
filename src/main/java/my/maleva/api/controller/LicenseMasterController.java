package my.maleva.api.controller;

import my.maleva.api.dto.LicenseMasterDto;
import my.maleva.api.service.LicenseMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/license-master")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class LicenseMasterController {

    private final LicenseMasterService service;

    public LicenseMasterController(LicenseMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<LicenseMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public LicenseMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<LicenseMasterDto> create(@Valid @RequestBody LicenseMasterDto dto) {
        LicenseMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/license-master/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public LicenseMasterDto update(@PathVariable Integer id, @Valid @RequestBody LicenseMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
