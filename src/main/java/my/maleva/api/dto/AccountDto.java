package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    private UUID id;

    @NotNull
    private Integer companyRefId;

    @NotBlank
    @Size(max = 20)
    private String accountCode;

    @Size(max = 100)
    private String description;

    private Integer chartSequence;

    @Size(max = 2)
    private String drcr;

    private Integer optimisticLockField;

    private Integer chartSequencePh;

    @Size(max = 100)
    private String accountCodePh;

    @NotNull
    private Integer rowIndex;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @Size(max = 50)
    private String modifiedBy;

    private Integer active;
}
