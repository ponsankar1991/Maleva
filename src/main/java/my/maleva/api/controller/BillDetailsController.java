package my.maleva.api.controller;

import my.maleva.api.dto.BillDetailsDto;
import my.maleva.api.service.BillDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bill-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class BillDetailsController {

    private final BillDetailsService service;

    public BillDetailsController(BillDetailsService service) {
        this.service = service;
    }

    @GetMapping
    public List<BillDetailsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public BillDetailsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<BillDetailsDto> create(@Valid @RequestBody BillDetailsDto dto) {
        BillDetailsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/bill-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public BillDetailsDto update(@PathVariable Integer id, @Valid @RequestBody BillDetailsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
