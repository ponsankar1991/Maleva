package my.maleva.api.module.jobs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusDetailsDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer jobMasterRefId;

    @NotNull
    private Integer status;

    @NotNull
    private Integer minStatus;

    @NotNull
    private Integer sort;

    private Integer masterStatus;
}
