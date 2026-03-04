package my.maleva.api.service;

import my.maleva.api.dto.JobTypeAllDataDto;

/**
 * Service interface for combined JobDetails and JobStatusDetails queries
 * Handles the SelectJobAllData operation that fetches both job details and job status details
 */
public interface JobTypeAllDataService {

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
    JobTypeAllDataDto selectJobAllData(Integer companyId, Integer jobId);
}

