package my.maleva.api.controller;

import my.maleva.api.dto.JobStatusMasterDto;
import my.maleva.api.service.JobStatusMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/job-status-master")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class JobStatusMasterController {

    private final JobStatusMasterService service;

    public JobStatusMasterController(JobStatusMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<JobStatusMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public JobStatusMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<JobStatusMasterDto> create(@Valid @RequestBody JobStatusMasterDto dto) {
        JobStatusMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/job-status-master/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public JobStatusMasterDto update(@PathVariable Integer id, @Valid @RequestBody JobStatusMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
