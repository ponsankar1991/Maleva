package my.maleva.api.module.master.dto;

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
public class LocationMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    @Size(max = 50)
    private String location;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotNull
    @Size(max = 50)
    private String modifiedBy;

    @NotNull
    private Integer active;
}
