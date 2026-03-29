package my.maleva.api.module.employee.controller;

import jakarta.validation.Valid;
import my.maleva.api.module.employee.dto.EmployeeMasterDto;
import my.maleva.api.module.employee.dto.EmployeeAllDto;
import my.maleva.api.module.employee.service.EmployeeMasterService;
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

    /**
     * Get all active employees for a company with optional employee type filter.
     * This is the equivalent of the .NET SelectEmployeeAll method.
     * Returns all employees (excluding Active=2) ordered by employee name.
     * Optionally filters by employee type if provided.
     *
     * @param companyRefId The company ID to filter by (required)
     * @param type The employee type to filter by (optional). If empty, "ALL", or not provided, returns all employees
     * @return List of all employees matching the criteria with their account information
     */
    @GetMapping("/company/{companyRefId}/all")
    public ResponseEntity<List<EmployeeAllDto>> selectEmployeeAll(
            @PathVariable Integer companyRefId,
            @RequestParam(value = "type", required = false, defaultValue = "ALL") String type) {
        List<EmployeeAllDto> employees = service.selectEmployeeAll(companyRefId, type);
        return ResponseEntity.ok(employees);
    }
}
