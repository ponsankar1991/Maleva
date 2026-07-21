package my.maleva.api.module.jobs.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.jobs.dto.JobTypeAllDataDto;
import my.maleva.api.module.jobs.service.JobTypeAllDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Collections;

/**
 * REST Controller for combined Job Data operations
 * Handles SelectJobAllData endpoint that returns both JobDetails and JobStatusDetails
 */
@RestController
@RequestMapping("/api/job-all-data")
@Validated
@PermitAll
public class JobTypeAllDataController {

    private final JobTypeAllDataService service;

    public JobTypeAllDataController(JobTypeAllDataService service) {
        this.service = service;
    }

    /**
     * Select all job data (job details + job status details) for a company and job
     * POST /api/job-all-data/select
     * Request body: { "companyId": 5, "jobId": 3 }
     *
     * @param companyId Company reference ID
     * @param jobId Job Master reference ID
     * @param complete Filter flag (0 = exclude status 8)
     * @return ApiResponse containing JobTypeAllDataDto with both job details and job status details lists
     */
    @PostMapping("/select")
    public ResponseEntity<ApiResponse<List<JobTypeAllDataDto>>> selectJobAllData(
            @RequestParam @NotNull Integer companyId,
            @RequestParam @NotNull Integer jobId,
            @RequestParam(required = false, defaultValue = "1") Integer complete) {
        try {
            // Validate inputs
            if (companyId <= 0 || jobId <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Company ID and Job ID must be greater than 0", HttpStatus.BAD_REQUEST.value()));
            }

            // Fetch combined job data
            JobTypeAllDataDto data = service.selectJobAllData(companyId, jobId, complete);

            // Check if data is empty
            if ((data.getJobTypeDetails() == null || data.getJobTypeDetails().isEmpty()) &&
                (data.getJobStatusDetails() == null || data.getJobStatusDetails().isEmpty())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No Job Data Found", HttpStatus.NOT_FOUND.value()));
            }

            return ResponseEntity.ok(
                    ApiResponse.success(Collections.singletonList(data), "Success"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
