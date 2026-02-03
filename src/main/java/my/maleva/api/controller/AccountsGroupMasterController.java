package my.maleva.api.controller;

import my.maleva.api.dto.AccountsGroupMasterDto;
import my.maleva.api.service.AccountsGroupMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/accounts-group")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class AccountsGroupMasterController {

    private final AccountsGroupMasterService service;

    public AccountsGroupMasterController(AccountsGroupMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<AccountsGroupMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public AccountsGroupMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<AccountsGroupMasterDto> create(@Valid @RequestBody AccountsGroupMasterDto dto) {
        AccountsGroupMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/accounts-group/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public AccountsGroupMasterDto update(@PathVariable Integer id, @Valid @RequestBody AccountsGroupMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
