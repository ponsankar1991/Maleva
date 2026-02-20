package my.maleva.api.controller;

import my.maleva.api.common.ApiResponse;
import my.maleva.api.dto.JobTypeAllDataDto;
import my.maleva.api.service.JobTypeAllDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;

/**
 * REST Controller for combined Job Data operations
 * Handles SelectJobAllData endpoint that returns both JobDetails and JobStatusDetails
 */
@RestController
@RequestMapping("/api/job-all-data")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100') or hasAuthority('ROLE_200')")
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
     * @return ApiResponse containing JobTypeAllDataDto with both job details and job status details lists
     */
    @PostMapping("/select")
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
            JobTypeAllDataDto data = service.selectJobAllData(companyId, jobId);

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

