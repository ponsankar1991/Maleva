package my.maleva.api.controller;

import my.maleva.api.dto.JobDetailsDto;
import my.maleva.api.service.JobDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/job-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class JobDetailsController {

    private final JobDetailsService service;

    public JobDetailsController(JobDetailsService service) {
        this.service = service;
    }

    @GetMapping
    public List<JobDetailsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public JobDetailsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<JobDetailsDto> create(@Valid @RequestBody JobDetailsDto dto) {
        JobDetailsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/job-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public JobDetailsDto update(@PathVariable Integer id, @Valid @RequestBody JobDetailsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
