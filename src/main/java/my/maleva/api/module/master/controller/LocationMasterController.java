package my.maleva.api.module.master.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.master.dto.LocationMasterDto;
import my.maleva.api.module.master.service.LocationMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/location-master")
@Validated
@PermitAll
public class LocationMasterController {

    private final LocationMasterService service;

    public LocationMasterController(LocationMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<LocationMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/company/{companyId}/active")
    public ResponseEntity<ApiResponse<List<LocationMasterDto>>> getActiveByCompany(@PathVariable Integer companyId) {
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Company ID must be a positive integer");
        }List<LocationMasterDto> locations = service.getActiveByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success("Active locations retrieved successfully", locations));
    }

    @GetMapping("/{id}")
    public LocationMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<LocationMasterDto> create(@Valid @RequestBody LocationMasterDto dto) {
        LocationMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/location-master/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public LocationMasterDto update(@PathVariable Integer id, @Valid @RequestBody LocationMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
