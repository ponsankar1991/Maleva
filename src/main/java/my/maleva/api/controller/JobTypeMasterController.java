package my.maleva.api.controller;

import my.maleva.api.dto.JobTypeMasterDto;
import my.maleva.api.service.JobTypeMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/job-type-master")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class JobTypeMasterController {

    private final JobTypeMasterService service;

    public JobTypeMasterController(JobTypeMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<JobTypeMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public JobTypeMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<JobTypeMasterDto> create(@Valid @RequestBody JobTypeMasterDto dto) {
        JobTypeMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/job-type-master/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public JobTypeMasterDto update(@PathVariable Integer id, @Valid @RequestBody JobTypeMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
