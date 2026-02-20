package my.maleva.api.service;

import my.maleva.api.dto.JobDetailsWithNameDto;
import my.maleva.api.dto.JobStatusDetailsWithNameDto;
import my.maleva.api.dto.JobTypeAllDataDto;
import my.maleva.api.repo.JobDetailsRepository;
import my.maleva.api.repo.JobStatusDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for combined JobDetails and JobStatusDetails queries
 * Handles the SelectJobAllData operation that fetches both job details and job status details
 */
@Service
public class JobTypeAllDataService {

    private final JobDetailsRepository jobDetailsRepository;
    private final JobStatusDetailsRepository jobStatusDetailsRepository;

    public JobTypeAllDataService(
            JobDetailsRepository jobDetailsRepository,
            JobStatusDetailsRepository jobStatusDetailsRepository) {
        this.jobDetailsRepository = jobDetailsRepository;
        this.jobStatusDetailsRepository = jobStatusDetailsRepository;
    }

    /**
     * Fetch all job data (details and status details) for a company and job
     * This combines:
     * - JobDetails with JobTypeMaster names
     * - JobStatusDetails with JobStatusMaster names
     *
     * @param companyId Company reference ID
     * @param jobId Job Master reference ID
     * @return JobTypeAllDataDto containing both lists
     */
    public JobTypeAllDataDto selectJobAllData(Integer companyId, Integer jobId) {
        if (companyId == null || companyId <= 0) {
            throw new IllegalArgumentException("Invalid company ID");
        }
        if (jobId == null || jobId <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        // Fetch job details with names
        List<JobDetailsWithNameDto> jobDetails = jobDetailsRepository.findJobDetailsWithNames(companyId, jobId);

        // Fetch job status details with names
        List<JobStatusDetailsWithNameDto> jobStatusDetails = jobStatusDetailsRepository.findJobStatusDetailsWithNames(companyId, jobId);

        // Return combined data
        return JobTypeAllDataDto.builder()
                .jobTypeDetails(jobDetails)
                .jobStatusDetails(jobStatusDetails)
                .build();
    }
}

