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

    /**
     * Get employees for a specific company filtered by role IDs.
     * Returns only active employees (Active=1).
     *
     * @param companyRefId The company ID to filter by (required)
     * @param roleId First role ID filter (optional)
     * @param roleId1 Second role ID filter (optional)
     * @return List of active employees matching the criteria
     */
    @GetMapping("/company/{companyRefId}/roles")
    public ResponseEntity<List<EmployeeMasterDto>> getEmployeesByCompanyAndRoles(
            @PathVariable Integer companyRefId,
            @RequestParam(value = "roleId", required = false) Integer roleId,
            @RequestParam(value = "roleId1", required = false) Integer roleId1) {
        List<EmployeeMasterDto> employees = service.getEmployeesByCompanyAndRoles(companyRefId, roleId, roleId1);
        return ResponseEntity.ok(employees);
    }
}
