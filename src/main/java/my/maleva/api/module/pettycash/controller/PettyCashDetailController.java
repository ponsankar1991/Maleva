package my.maleva.api.module.pettycash.controller;

import my.maleva.api.module.pettycash.dto.PettyCashDetailDto;
import my.maleva.api.module.pettycash.service.PettyCashDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/petty-cash-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PettyCashDetailController {

    private final PettyCashDetailService service;

    public PettyCashDetailController(PettyCashDetailService service) {
        this.service = service;
    }

    @GetMapping
    public List<PettyCashDetailDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PettyCashDetailDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PettyCashDetailDto> create(@Valid @RequestBody PettyCashDetailDto dto) {
        PettyCashDetailDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/petty-cash-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PettyCashDetailDto update(@PathVariable Integer id, @Valid @RequestBody PettyCashDetailDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
