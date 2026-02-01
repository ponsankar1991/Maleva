package my.maleva.api.controller;

import jakarta.validation.Valid;
import my.maleva.api.dto.EmployeeMasterDto;
import my.maleva.api.service.EmployeeMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeMasterController {

    private final EmployeeMasterService service;

    public EmployeeMasterController(EmployeeMasterService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmployeeMasterDto> create(@Valid @RequestBody EmployeeMasterDto dto) {
        EmployeeMasterDto created = service.create(dto);
        return ResponseEntity.created(URI.create("/api/employees/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeMasterDto> update(@PathVariable Integer id, @Valid @RequestBody EmployeeMasterDto dto) {
        EmployeeMasterDto updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeMasterDto> get(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeMasterDto>> list(@RequestParam(value = "name", required = false) String name) {
        return ResponseEntity.ok(service.findAll(name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
