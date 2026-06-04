package my.maleva.api.module.company.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.company.dto.MasterSettingDto;
import my.maleva.api.module.company.service.MasterSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/master-settings")
@Validated

@PermitAll
public class MasterSettingController {

    private final MasterSettingService service;

    public MasterSettingController(MasterSettingService service) {
        this.service = service;
    }

    @GetMapping
    public List<MasterSettingDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public MasterSettingDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<MasterSettingDto> create(@Valid @RequestBody MasterSettingDto dto) {
        MasterSettingDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/master-settings/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public MasterSettingDto update(@PathVariable Integer id, @Valid @RequestBody MasterSettingDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
