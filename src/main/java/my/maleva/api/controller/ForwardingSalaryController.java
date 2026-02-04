package my.maleva.api.controller;

import my.maleva.api.dto.ForwardingSalaryDto;
import my.maleva.api.service.ForwardingSalaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/forwarding-salaries")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class ForwardingSalaryController {

    private final ForwardingSalaryService service;

    public ForwardingSalaryController(ForwardingSalaryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ForwardingSalaryDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public ForwardingSalaryDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<ForwardingSalaryDto> create(@Valid @RequestBody ForwardingSalaryDto dto) {
        ForwardingSalaryDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/forwarding-salaries/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ForwardingSalaryDto update(@PathVariable Integer id, @Valid @RequestBody ForwardingSalaryDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
