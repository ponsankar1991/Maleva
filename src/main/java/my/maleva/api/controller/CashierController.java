package my.maleva.api.controller;

import my.maleva.api.dto.CashierDto;
import my.maleva.api.service.CashierService;
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
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
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
