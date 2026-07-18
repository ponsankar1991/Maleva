package my.maleva.api.module.employee.controller;

import jakarta.validation.Valid;
import my.maleva.api.module.employee.dto.EmployeeMasterDto;
import my.maleva.api.module.employee.dto.EmployeeAllDto;
import my.maleva.api.module.employee.dto.EmployeeSearchRequest;
import my.maleva.api.module.employee.dto.EmployeeSearchResponse;
import my.maleva.api.module.employee.dto.EmployeeTypeDto;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.common.constant.UserRoles;
import my.maleva.api.module.employee.service.EmployeeMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URI;
import java.util.ArrayList;
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
    public ResponseEntity<List<EmployeeAllDto>> selectEmployeeAll( @PathVariable Integer companyRefId, @RequestParam(value = "type", required = false, defaultValue = "ALL") String type) {
        List<EmployeeAllDto> employees = service.selectEmployeeAll(companyRefId, type);
        return ResponseEntity.ok(employees);
    }
    /**
     * Search employees with dynamic filtering matching legacy SelectEmployee endpoint.
     *
     * @param request The search parameters
     * @return ApiResponse containing the list of employees and total count
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<EmployeeSearchResponse>> searchEmployees(@RequestBody EmployeeSearchRequest request) {
        EmployeeSearchResponse response = service.searchEmployees(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employees fetched successfully"));
    }

    /**
     * Get a list of available employee types (roles) matching the legacy SelectEmployeeType endpoint.
     * Maps to the application's UserRoles enum.
     *
     * @param includeAll Whether to include the "ALL" option with ID 0
     * @return ApiResponse containing the list of roles
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<EmployeeTypeDto>>> getEmployeeTypes(
            @RequestParam(value = "all", defaultValue = "false") boolean includeAll) {
        
        List<EmployeeTypeDto> types = new ArrayList<>();
        
        if (includeAll) {
            types.add(new EmployeeTypeDto(0, "ALL"));
        }
        
        for (UserRoles role : UserRoles.values()) {
            types.add(new EmployeeTypeDto(role.getRoleId(), getLegacyRoleName(role)));
        }
        
        return ResponseEntity.ok(ApiResponse.success(types, "Employee types fetched successfully"));
    }

    private String getLegacyRoleName(UserRoles role) {
        switch (role) {
            case CUSTOMERSERVICE:
                return "CustomerServiceAdmin";
            case BOARDINGOFFICER:
                return "BOARDING";
            default:
                return role.name();
        }
    }

    /**
     * Bulk insert/update employees. Replicates the legacy SP_Employee logic.
     * Maps to the .NET InsertEmployee endpoint.
     *
     * @param companyRefId The company ID
     * @param employees    List of employees to upsert
     * @return ApiResponse containing status and last processed ID
     */
    @PostMapping("/bulk/{companyRefId}")
    public ResponseEntity<ApiResponse<String>> bulkUpsertEmployees(
            @PathVariable Integer companyRefId,
            @RequestBody List<EmployeeMasterDto> employees) {
        ApiResponse<String> response = service.bulkUpsertEmployees(employees, companyRefId);
        return ResponseEntity.ok(response);
    }
}
