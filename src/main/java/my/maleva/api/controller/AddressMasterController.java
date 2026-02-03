package my.maleva.api.controller;

import my.maleva.api.dto.AddressMasterDto;
import my.maleva.api.service.AddressMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class AddressMasterController {

    private final AddressMasterService service;

    public AddressMasterController(AddressMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<AddressMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public AddressMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<AddressMasterDto> create(@Valid @RequestBody AddressMasterDto dto) {
        AddressMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/addresses/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public AddressMasterDto update(@PathVariable Integer id, @Valid @RequestBody AddressMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
