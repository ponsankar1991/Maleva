package my.maleva.api.module.jobs.service.impl;

import my.maleva.api.module.jobs.dto.JobTypeAllDataDto;
import my.maleva.api.module.jobs.dto.JobDetailsWithNameDto;
import my.maleva.api.module.jobs.dto.JobStatusDetailsWithNameDto;
import my.maleva.api.module.jobs.repository.JobDetailsRepository;
import my.maleva.api.module.jobs.repository.JobStatusDetailsRepository;
import my.maleva.api.module.jobs.service.JobTypeAllDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JobTypeAllDataServiceImpl - Implementation for JobTypeAllDataService
 *
 * Retrieves and combines JobDetails and JobStatusDetails
 * equivalent to the .NET SelectJobAllData method
 */
@Service
public class JobTypeAllDataServiceImpl implements JobTypeAllDataService {

    private static final Logger logger = LoggerFactory.getLogger(JobTypeAllDataServiceImpl.class);

    @Autowired
    private JobDetailsRepository jobDetailsRepository;

    @Autowired
    private JobStatusDetailsRepository jobStatusDetailsRepository;

    public JobTypeAllDataServiceImpl(
            JobDetailsRepository jobDetailsRepository,
            JobStatusDetailsRepository jobStatusDetailsRepository) {
        this.jobDetailsRepository = jobDetailsRepository;
        this.jobStatusDetailsRepository = jobStatusDetailsRepository;
    }

    @Override
    public JobTypeAllDataDto selectJobAllData(Integer comid, Integer jobid, Integer complete) {
        logger.info("Selecting Job All Data for Company: {} Job: {} Complete: {}", comid, jobid, complete);

        // Validate inputs
        if (comid == null || comid <= 0) {
            throw new IllegalArgumentException("Invalid company ID");
        }
        if (jobid == null || jobid <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }
        if (complete == null) {
            complete = 1; // Default logic
        }

        JobTypeAllDataDto result = new JobTypeAllDataDto();

        try {
            // Fetch JobDetails with joined information
            // Returns JobDetailsWithNameDto which already includes jobName and statusName from joins
            List<JobDetailsWithNameDto> jobDetailsList = jobDetailsRepository.findJobDetailsWithNames(comid, jobid);
            result.setJobTypeDetails(jobDetailsList);
            logger.info("Retrieved {} Job Details records", jobDetailsList.size());

            // Fetch JobStatusDetails with joined information and filter
            // Returns JobStatusDetailsWithNameDto which already includes statusName and minStatusName from joins
            List<JobStatusDetailsWithNameDto> jobStatusDetailsList = jobStatusDetailsRepository.findJobStatusDetailsWithNames(comid, jobid, complete);
            result.setJobStatusDetails(jobStatusDetailsList);
            logger.info("Retrieved {} Job Status Details records", jobStatusDetailsList.size());

        } catch (IllegalArgumentException ex) {
            logger.error("Validation error for Company: {} Job: {}", comid, jobid);
            throw ex;
        } catch (Exception ex) {
            logger.error("Error while selecting Job All Data for Company: {} Job: {}", comid, jobid, ex);
            throw new RuntimeException("Failed to select job data: " + ex.getMessage(), ex);
        }

        return result;
    }

    // Retained for backward compatibility
    @Override
    public JobTypeAllDataDto selectJobAllData(Integer comid, Integer jobid) {
        return selectJobAllData(comid, jobid, 1); 
    }
}
