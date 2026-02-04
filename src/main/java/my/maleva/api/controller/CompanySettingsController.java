package my.maleva.api.controller;

import my.maleva.api.dto.CompanySettingsDto;
import my.maleva.api.service.CompanySettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/company-settings")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CompanySettingsController {

    private final CompanySettingsService service;

    public CompanySettingsController(CompanySettingsService service) {
        this.service = service;
    }

    @GetMapping
    public List<CompanySettingsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CompanySettingsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @GetMapping("/company/{companyRefId}")
    public CompanySettingsDto getByCompanyRefId(@PathVariable Integer companyRefId) {
        return service.getByCompanyRefId(companyRefId);
    }

    @PostMapping
    public ResponseEntity<CompanySettingsDto> create(@Valid @RequestBody CompanySettingsDto dto) {
        CompanySettingsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/company-settings/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CompanySettingsDto update(@PathVariable Integer id, @Valid @RequestBody CompanySettingsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
