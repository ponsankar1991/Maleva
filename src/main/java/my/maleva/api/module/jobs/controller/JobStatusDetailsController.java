package my.maleva.api.module.jobs.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.jobs.dto.JobStatusDetailsDto;
import my.maleva.api.module.jobs.service.JobStatusDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/job-status-details")
@Validated
@PermitAll
public class JobStatusDetailsController {

    private final JobStatusDetailsService service;

    public JobStatusDetailsController(JobStatusDetailsService service) {
        this.service = service;
    }

    @GetMapping
    public List<JobStatusDetailsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public JobStatusDetailsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<JobStatusDetailsDto> create(@Valid @RequestBody JobStatusDetailsDto dto) {
        JobStatusDetailsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/job-status-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public JobStatusDetailsDto update(@PathVariable Integer id, @Valid @RequestBody JobStatusDetailsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
