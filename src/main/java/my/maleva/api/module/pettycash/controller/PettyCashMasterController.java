package my.maleva.api.module.pettycash.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.pettycash.dto.PettyCashMasterDto;
import my.maleva.api.module.pettycash.service.PettyCashMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/petty-cash-masters")
@Validated
@PermitAll
public class PettyCashMasterController {

    private final PettyCashMasterService service;

    public PettyCashMasterController(PettyCashMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<PettyCashMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PettyCashMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PettyCashMasterDto> create(@Valid @RequestBody PettyCashMasterDto dto) {
        PettyCashMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/petty-cash-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PettyCashMasterDto update(@PathVariable Integer id, @Valid @RequestBody PettyCashMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
