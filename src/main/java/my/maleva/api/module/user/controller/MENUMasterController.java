package my.maleva.api.module.user.controller;

import my.maleva.api.module.user.dto.MENUMasterDto;
import my.maleva.api.module.user.service.MENUMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/menu-master")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class MENUMasterController {

    private final MENUMasterService service;

    public MENUMasterController(MENUMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<MENUMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public MENUMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<MENUMasterDto> create(@Valid @RequestBody MENUMasterDto dto) {
        MENUMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/menu-master/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public MENUMasterDto update(@PathVariable Integer id, @Valid @RequestBody MENUMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
