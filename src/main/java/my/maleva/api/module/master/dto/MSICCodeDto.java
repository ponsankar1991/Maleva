package my.maleva.api.module.master.dto;

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
public class MSICCodeDto {
    private Integer id;

    @Size(max = 50)
    private String msicCode;

    @NotNull
    @Size(max = 250)
    private String description;
}
