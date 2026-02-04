package my.maleva.api.controller;

import my.maleva.api.dto.BillsOrderDetailsDto;
import my.maleva.api.service.BillsOrderDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bills-order-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class BillsOrderDetailsController {

    private final BillsOrderDetailsService service;

    public BillsOrderDetailsController(BillsOrderDetailsService service) {
        this.service = service;
    }

    @GetMapping
    public List<BillsOrderDetailsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public BillsOrderDetailsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<BillsOrderDetailsDto> create(@Valid @RequestBody BillsOrderDetailsDto dto) {
        BillsOrderDetailsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/bills-order-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public BillsOrderDetailsDto update(@PathVariable Integer id, @Valid @RequestBody BillsOrderDetailsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
