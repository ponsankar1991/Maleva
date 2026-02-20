package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enhanced JobDetails DTO that includes job name and status name from joined tables
 * Maps to: JobDetails joined with JobTypeMaster and JobStatusMaster
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDetailsWithNameDto {
    private Integer id;
    private Integer jobMasterRefId;
    private String description;
    private String jobName;        // from JobTypeMaster.Name
    private String statusName;     // from JobStatusMaster.Name (for Status field)
    private Integer active;
    private Integer mandatory;
    private Integer status;
}

