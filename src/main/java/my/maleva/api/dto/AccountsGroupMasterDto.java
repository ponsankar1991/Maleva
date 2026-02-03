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
public class AccountsGroupMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotBlank
    @Size(max = 500)
    private String accountName;

    private Integer parentId;

    @NotNull
    private Boolean editmode;

    private Integer noChild;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 200)
    private String modifiedBy;

    @NotNull
    private Integer active;

    @NotBlank
    @Size(max = 20)
    private String accountCode;

    @Size(max = 50)
    private String updateId;

    @Size(max = 50)
    private String qneCode;
}
