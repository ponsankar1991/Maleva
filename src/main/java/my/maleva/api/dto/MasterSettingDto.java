package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterSettingDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    @Size(max = 50)
    private String variableName;

    @NotNull
    private Boolean status;

    private LocalDateTime createdDate;

    @NotNull
    @Size(max = 50)
    private String modifiedBy;

    private LocalDateTime modifiedDate;

    @Size(max = 3800)
    private String sValue;
}
