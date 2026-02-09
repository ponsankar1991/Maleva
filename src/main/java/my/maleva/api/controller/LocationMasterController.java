package my.maleva.api.controller;

import my.maleva.api.dto.LocationMasterDto;
import my.maleva.api.service.LocationMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/location-master")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class LocationMasterController {

    private final LocationMasterService service;

    public LocationMasterController(LocationMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<LocationMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public LocationMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<LocationMasterDto> create(@Valid @RequestBody LocationMasterDto dto) {
        LocationMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/location-master/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public LocationMasterDto update(@PathVariable Integer id, @Valid @RequestBody LocationMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
