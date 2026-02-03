package my.maleva.api.controller;

import my.maleva.api.dto.AccountDto;
import my.maleva.api.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountDto> list() {
        return accountService.listAll();
    }

    @GetMapping("/{id}")
    public AccountDto get(@PathVariable UUID id) {
        return accountService.getById(id);
    }

    @PostMapping
    public ResponseEntity<AccountDto> create(@Valid @RequestBody AccountDto dto) {
        AccountDto saved = accountService.create(dto);
        return ResponseEntity.created(URI.create("/api/accounts/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public AccountDto update(@PathVariable UUID id, @Valid @RequestBody AccountDto dto) {
        return accountService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
