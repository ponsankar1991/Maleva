package my.maleva.api.controller;

import my.maleva.api.dto.BankMasterDto;
import my.maleva.api.service.BankMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/banks")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class BankMasterController {

    private final BankMasterService service;

    public BankMasterController(BankMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<BankMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public BankMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<BankMasterDto> create(@Valid @RequestBody BankMasterDto dto) {
        BankMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/banks/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public BankMasterDto update(@PathVariable Integer id, @Valid @RequestBody BankMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
