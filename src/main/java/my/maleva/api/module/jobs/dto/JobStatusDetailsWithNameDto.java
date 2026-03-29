package my.maleva.api.module.jobs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enhanced JobStatusDetails DTO that includes status names from joined tables
 * Maps to: JobStatusDetails joined with JobStatusMaster (for both Status and MinStatus)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusDetailsWithNameDto {
    private Integer id;
    private Integer jobMasterRefId;
    private Integer status;
    private String statusName;     // from JobStatusMaster.Name (for Status field)
    private Integer minStatus;
    private String minStatusName;  // from JobStatusMaster.Name (for MinStatus field)
    private Integer sort;
}

