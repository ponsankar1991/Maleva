package my.maleva.api.controller;

import my.maleva.api.dto.DriverMasterDto;
import my.maleva.api.service.DriverMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/driver-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class DriverMasterController {

    private final DriverMasterService service;

    public DriverMasterController(DriverMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<DriverMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public DriverMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<DriverMasterDto> create(@Valid @RequestBody DriverMasterDto dto) {
        DriverMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/driver-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public DriverMasterDto update(@PathVariable Integer id, @Valid @RequestBody DriverMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
