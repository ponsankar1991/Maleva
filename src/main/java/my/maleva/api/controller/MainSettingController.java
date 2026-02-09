package my.maleva.api.controller;

import my.maleva.api.dto.MainSettingDto;
import my.maleva.api.service.MainSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/main-settings")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class MainSettingController {

    private final MainSettingService service;

    public MainSettingController(MainSettingService service) {
        this.service = service;
    }

    @GetMapping
    public List<MainSettingDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public MainSettingDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<MainSettingDto> create(@Valid @RequestBody MainSettingDto dto) {
        MainSettingDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/main-settings/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public MainSettingDto update(@PathVariable Integer id, @Valid @RequestBody MainSettingDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
