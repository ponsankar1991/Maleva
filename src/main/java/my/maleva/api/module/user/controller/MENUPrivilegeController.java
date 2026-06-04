package my.maleva.api.module.user.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.user.dto.MENUPrivilegeDto;
import my.maleva.api.module.user.service.MENUPrivilegeService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/menu-privileges")
@Validated
@PermitAll
public class MENUPrivilegeController {

    private final MENUPrivilegeService service;

    public MENUPrivilegeController(MENUPrivilegeService service) {
        this.service = service;
    }

    @GetMapping
    public List<MENUPrivilegeDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public MENUPrivilegeDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<MENUPrivilegeDto> create(@Valid @RequestBody MENUPrivilegeDto dto) {
        MENUPrivilegeDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/menu-privileges/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public MENUPrivilegeDto update(@PathVariable Integer id, @Valid @RequestBody MENUPrivilegeDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
