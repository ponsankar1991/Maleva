package my.maleva.api.service.impl;

import my.maleva.api.dto.JobTypeAllDataDto;
import my.maleva.api.dto.JobDetailsWithNameDto;
import my.maleva.api.dto.JobStatusDetailsWithNameDto;
import my.maleva.api.repo.JobDetailsRepository;
import my.maleva.api.repo.JobStatusDetailsRepository;
import my.maleva.api.service.JobTypeAllDataService;
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
    public JobTypeAllDataDto selectJobAllData(Integer comid, Integer jobid) {
        logger.info("Selecting Job All Data for Company: {} Job: {}", comid, jobid);

        // Validate inputs
        if (comid == null || comid <= 0) {
            throw new IllegalArgumentException("Invalid company ID");
        }
        if (jobid == null || jobid <= 0) {
            throw new IllegalArgumentException("Invalid job ID");
        }

        JobTypeAllDataDto result = new JobTypeAllDataDto();

        try {
            // Fetch JobDetails with joined information
            // Returns JobDetailsWithNameDto which already includes jobName and statusName from joins
            List<JobDetailsWithNameDto> jobDetailsList = jobDetailsRepository.findJobDetailsWithNames(comid, jobid);
            result.setJobTypeDetails(jobDetailsList);
            logger.info("Retrieved {} Job Details records", jobDetailsList.size());

            // Fetch JobStatusDetails with joined information
            // Returns JobStatusDetailsWithNameDto which already includes statusName and minStatusName from joins
            List<JobStatusDetailsWithNameDto> jobStatusDetailsList = jobStatusDetailsRepository.findJobStatusDetailsWithNames(comid, jobid);
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
}

