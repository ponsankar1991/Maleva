package my.maleva.api.module.jobs.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.jobs.dto.JobStatusMasterDto;
import my.maleva.api.module.jobs.service.JobStatusMasterService;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/job-status-master")
@Validated
@PermitAll
public class JobStatusMasterController {

    private final JobStatusMasterService service;

    public JobStatusMasterController(JobStatusMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<JobStatusMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/select/{companyId}/")
    public ResponseEntity<ApiResponse<List<JobStatusMasterDto>>> getByCompanyActive(@PathVariable @NotNull Integer companyId) {
        try {
            List<JobStatusMasterDto> list = service.selectJobStatus(companyId);

            if (list.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure(HttpStatus.NOT_FOUND, "No Job Statuses Found for company ID: " + companyId));
            }

            return ResponseEntity.ok(ApiResponse.success("Job Statuses Retrieved Successfully", list));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve job statuses"));
        }
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
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
