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
public class MENUMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    @Size(max = 100)
    private String formText;

    @Size(max = 100)
    private String formName;

    @NotNull
    private Integer show;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotNull
    @Size(max = 50)
    private String modifiedBy;

    private Integer parentId;
}
