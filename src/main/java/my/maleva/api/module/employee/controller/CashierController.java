package my.maleva.api.module.employee.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.employee.dto.CashierDto;
import my.maleva.api.module.employee.service.CashierService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cashiers")
@Validated
@PermitAll
public class CashierController {

    private final CashierService service;

    public CashierController(CashierService service) {
        this.service = service;
    }

    @GetMapping
    public List<CashierDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CashierDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CashierDto> create(@Valid @RequestBody CashierDto dto) {
        CashierDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/cashiers/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CashierDto update(@PathVariable Integer id, @Valid @RequestBody CashierDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
