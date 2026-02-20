package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Combined DTO that wraps both JobDetails and JobStatusDetails with their names
 * This is the response model for SelectJobAllData endpoint
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobTypeAllDataDto {
    private List<JobDetailsWithNameDto> jobTypeDetails;
    private List<JobStatusDetailsWithNameDto> jobStatusDetails;
}

