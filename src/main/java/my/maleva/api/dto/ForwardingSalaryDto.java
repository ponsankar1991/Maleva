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
public class ForwardingSalaryDto {
    private Integer id;

    private Integer companyRefId;

    @NotNull
    private Integer rtiMasterRefId;

    private Integer employeeMasterRefId;
    private Integer employeeMasterRefId1;

    @NotNull
    private LocalDateTime createdDate;

    private Float salary1;
    private Float salary2;
}
