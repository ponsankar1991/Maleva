package my.maleva.api.module.company.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.company.dto.CompanyDto;
import my.maleva.api.module.company.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Validated
@PermitAll
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    public List<CompanyDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CompanyDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CompanyDto> create(@Valid @RequestBody CompanyDto dto) {
        CompanyDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/companies/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CompanyDto update(@PathVariable Integer id, @Valid @RequestBody CompanyDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
