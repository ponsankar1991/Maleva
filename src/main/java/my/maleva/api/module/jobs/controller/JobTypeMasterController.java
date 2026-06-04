package my.maleva.api.module.jobs.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.jobs.dto.JobTypeMasterDto;
import my.maleva.api.module.jobs.dto.JobTypeAllDataDto;
import my.maleva.api.module.jobs.service.JobTypeMasterService;
import my.maleva.api.module.jobs.service.JobTypeAllDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

/**
 * REST Controller for Job Type Master operations
 * Handles CRUD operations and filtering for Job Types
 */
@RestController
@RequestMapping("/api/job-type-master")
@Validated

@PermitAll
public class JobTypeMasterController {

    private final JobTypeMasterService service;
    private final JobTypeAllDataService jobTypeAllDataService;

    public JobTypeMasterController(JobTypeMasterService service, JobTypeAllDataService jobTypeAllDataService) {
        this.service = service;
        this.jobTypeAllDataService = jobTypeAllDataService;
    }

    /**
     * Get all job types
     * @return List of all job types
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobTypeMasterDto>>> list() {
        List<JobTypeMasterDto> jobTypes = service.listAll();

        if (jobTypes.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No Job Types Found", jobTypes));
        }

        return ResponseEntity.ok(ApiResponse.success("Job Types Retrieved Successfully", jobTypes));
    }

    /**
     * Get job types by company ID
     * @param companyId Company ID to filter by
     * @return Job types for the specified company
     */
    @GetMapping("/jobtypes/{companyId}")
    public ResponseEntity<ApiResponse<List<JobTypeMasterDto>>> getByCompanyId(
            @PathVariable @NotNull Integer companyId) {
        return service.getJobTypes(companyId);
    }

    /**
     * Get job type by ID
     * @param id Job Type ID
     * @return Job type details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobTypeMasterDto>> get(@PathVariable @NotNull Integer id) {
        try {
            JobTypeMasterDto jobType = service.getById(id);
            return ResponseEntity.ok(ApiResponse.success("Job Type Retrieved Successfully", jobType));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(HttpStatus.NOT_FOUND, e.getMessage()));
        }
    }

    /**
     * Create a new job type
     * @param dto Job Type data to create
     * @return Created job type with HTTP 201
     */
    @PostMapping
    public ResponseEntity<ApiResponse<JobTypeMasterDto>> create(@Valid @RequestBody JobTypeMasterDto dto) {
        try {
            JobTypeMasterDto saved = service.create(dto);
            return ResponseEntity
                    .created(URI.create("/api/job-type-master/" + saved.getId()))
                    .body(ApiResponse.success("Job Type Created Successfully", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * Update an existing job type
     * @param id Job Type ID to update
     * @param dto Updated job type data
     * @return Updated job type
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobTypeMasterDto>> update(
            @PathVariable @NotNull Integer id,
            @Valid @RequestBody JobTypeMasterDto dto) {
        try {
            JobTypeMasterDto updated = service.update(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Job Type Updated Successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(HttpStatus.NOT_FOUND, e.getMessage()));
        }
    }

    /**
     * Delete a job type by ID
     * @param id Job Type ID to delete
     * @return HTTP 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Select all job data (job details + job status details) for a company and job
     * Equivalent to .NET SelectJobAllData endpoint
     * POST /api/job-type-master/select-all-data?companyId=1&jobId=5
     *
     * @param companyId Company reference ID
     * @param jobId Job Master reference ID
     * @return ApiResponse containing JobTypeAllDataDto with both job details and job status details lists
     */
    @PostMapping("/select-all-data")
    public ResponseEntity<ApiResponse<JobTypeAllDataDto>> selectJobAllData(
            @RequestParam @NotNull Integer companyId,
            @RequestParam @NotNull Integer jobId) {
        try {
            // Validate inputs
            if (companyId <= 0 || jobId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, "Company ID and Job ID must be greater than 0"));
            }

            // Fetch combined job data
            JobTypeAllDataDto data = jobTypeAllDataService.selectJobAllData(companyId, jobId);

            // Check if data is empty
            if ((data.getJobTypeDetails() == null || data.getJobTypeDetails().isEmpty()) &&
                (data.getJobStatusDetails() == null || data.getJobStatusDetails().isEmpty())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure(HttpStatus.NOT_FOUND, "No Job Data Found"));
            }

            return ResponseEntity.ok(
                    ApiResponse.success("Job Data Retrieved Successfully", data));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(HttpStatus.BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve job data"));
        }
    }
}
