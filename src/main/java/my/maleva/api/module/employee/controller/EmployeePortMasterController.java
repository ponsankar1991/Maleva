package my.maleva.api.module.employee.controller;

import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.employee.dto.EmployeePortMasterDto;
import my.maleva.api.module.employee.service.EmployeePortMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employee-ports")
@Validated
public class EmployeePortMasterController {

    private final EmployeePortMasterService service;

    public EmployeePortMasterController(EmployeePortMasterService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeePortMasterDto>> create(@Valid @RequestBody EmployeePortMasterDto dto) {
        try {
            EmployeePortMasterDto created = service.create(dto);
            return ResponseEntity
                    .created(URI.create("/api/employee-ports/" + created.getId()))
                    .body(ApiResponse.success(created, "Employee Port successfully assigned."));
        } catch (Exception e) {
            // The @Transactional annotation on the service layer will automatically rollback
            // the database if an exception occurs. Here we catch it to return a clean API response.
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to assign Employee Port: " + e.getMessage(), 500));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<EmployeePortMasterDto>>> bulkCreate(@Valid @RequestBody List<EmployeePortMasterDto> dtos) {
        try {
            List<EmployeePortMasterDto> created = service.bulkCreate(dtos);
            return ResponseEntity.ok(ApiResponse.success(created, "Employee Ports successfully assigned in bulk."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to bulk assign Employee Ports: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/company/{companyRefId}/employee/{employeeRefId}")
    public ResponseEntity<ApiResponse<List<EmployeePortMasterDto>>> getByEmployee(
            @PathVariable Integer companyRefId, 
            @PathVariable Integer employeeRefId) {
        try {
            List<EmployeePortMasterDto> ports = service.getByEmployeeRefId(companyRefId, employeeRefId);
            return ResponseEntity.ok(ApiResponse.success(ports, "Employee ports fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch Employee ports: " + e.getMessage(), 500));
        }
    }
}
