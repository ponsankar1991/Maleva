package my.maleva.api.controller;

import my.maleva.api.dto.FormTransactionPasswordDto;
import my.maleva.api.service.FormTransactionPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/form-transaction-passwords")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class FormTransactionPasswordController {

    private final FormTransactionPasswordService service;

    public FormTransactionPasswordController(FormTransactionPasswordService service) {
        this.service = service;
    }

    @GetMapping
    public List<FormTransactionPasswordDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public FormTransactionPasswordDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<FormTransactionPasswordDto> create(@Valid @RequestBody FormTransactionPasswordDto dto) {
        FormTransactionPasswordDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/form-transaction-passwords/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public FormTransactionPasswordDto update(@PathVariable Integer id, @Valid @RequestBody FormTransactionPasswordDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
