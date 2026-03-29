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
public class ItemMasterJobDetailsDto {
    private Integer id;

    @NotNull
    private Integer itemMasterRefId;

    @NotNull
    private Integer jobMasterRefId;

    @NotNull
    private Integer active;
}
