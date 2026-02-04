package my.maleva.api.controller;

import my.maleva.api.dto.CountryMasterDto;
import my.maleva.api.service.CountryMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/country-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CountryMasterController {

    private final CountryMasterService service;

    public CountryMasterController(CountryMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<CountryMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CountryMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CountryMasterDto> create(@Valid @RequestBody CountryMasterDto dto) {
        CountryMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/country-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CountryMasterDto update(@PathVariable Integer id, @Valid @RequestBody CountryMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
