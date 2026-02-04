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
public class BankMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String accountName;

    @NotBlank
    @Size(max = 100)
    private String accountNo;

    @NotBlank
    @Size(max = 100)
    private String ifscCode;

    @NotBlank
    @Size(max = 100)
    private String branch;

    @NotNull
    private Integer dFlag;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @NotNull
    private Integer active;

    @NotNull
    private Integer accountRefid;

    @Size(max = 50)
    private String qneCode;

    @Size(max = 50)
    private String qneId;
}
