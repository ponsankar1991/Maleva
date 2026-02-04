package my.maleva.api.controller;

import my.maleva.api.dto.ClaimVoucherDto;
import my.maleva.api.service.ClaimVoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/claim-vouchers")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ClaimVoucherController {

    private final ClaimVoucherService service;

    public ClaimVoucherController(ClaimVoucherService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClaimVoucherDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ClaimVoucherDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ClaimVoucherDto> create(@Valid @RequestBody ClaimVoucherDto dto) {
        ClaimVoucherDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/claim-vouchers/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ClaimVoucherDto update(@PathVariable Integer id, @Valid @RequestBody ClaimVoucherDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
