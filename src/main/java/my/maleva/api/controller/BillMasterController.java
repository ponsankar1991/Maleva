package my.maleva.api.controller;

import my.maleva.api.dto.BillMasterDto;
import my.maleva.api.service.BillMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class BillMasterController {

    private final BillMasterService service;

    public BillMasterController(BillMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<BillMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public BillMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<BillMasterDto> create(@Valid @RequestBody BillMasterDto dto) {
        BillMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/bills/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public BillMasterDto update(@PathVariable Integer id, @Valid @RequestBody BillMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
