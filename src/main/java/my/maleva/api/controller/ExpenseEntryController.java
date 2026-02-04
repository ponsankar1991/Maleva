package my.maleva.api.controller;

import my.maleva.api.dto.ExpenseEntryDto;
import my.maleva.api.service.ExpenseEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/expense-entries")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ExpenseEntryController {

    private final ExpenseEntryService service;

    public ExpenseEntryController(ExpenseEntryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExpenseEntryDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ExpenseEntryDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ExpenseEntryDto> create(@Valid @RequestBody ExpenseEntryDto dto) {
        ExpenseEntryDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/expense-entries/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ExpenseEntryDto update(@PathVariable Integer id, @Valid @RequestBody ExpenseEntryDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
