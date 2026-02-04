package my.maleva.api.controller;

import my.maleva.api.dto.BillsOrderMasterDto;
import my.maleva.api.service.BillsOrderMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bills-order")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class BillsOrderMasterController {

    private final BillsOrderMasterService service;

    public BillsOrderMasterController(BillsOrderMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<BillsOrderMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public BillsOrderMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<BillsOrderMasterDto> create(@Valid @RequestBody BillsOrderMasterDto dto) {
        BillsOrderMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/bills-order/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public BillsOrderMasterDto update(@PathVariable Integer id, @Valid @RequestBody BillsOrderMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
