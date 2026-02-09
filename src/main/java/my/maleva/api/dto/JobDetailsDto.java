package my.maleva.api.dto;

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
public class JobDetailsDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer jobMasterRefId;

    @NotNull
    @Size(max = 100)
    private String description;

    @NotNull
    private Integer active;

    @NotNull
    private Integer mandatory;

    @NotNull
    private Integer status;
}
