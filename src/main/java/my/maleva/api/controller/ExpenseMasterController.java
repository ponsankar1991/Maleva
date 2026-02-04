package my.maleva.api.controller;

import my.maleva.api.dto.ExpenseMasterDto;
import my.maleva.api.service.ExpenseMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/expense-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ExpenseMasterController {

    private final ExpenseMasterService service;

    public ExpenseMasterController(ExpenseMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExpenseMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ExpenseMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ExpenseMasterDto> create(@Valid @RequestBody ExpenseMasterDto dto) {
        ExpenseMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/expense-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ExpenseMasterDto update(@PathVariable Integer id, @Valid @RequestBody ExpenseMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
