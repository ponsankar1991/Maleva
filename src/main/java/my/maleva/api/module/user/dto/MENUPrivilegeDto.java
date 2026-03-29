package my.maleva.api.module.user.dto;

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
public class MENUPrivilegeDto {
    private Integer id;

    @NotNull
    private Integer userRefId;

    @NotNull
    @Size(max = 100)
    private String formText;

    @Size(max = 100)
    private String formName;

    private Integer parentId;

    @NotNull
    @Size(max = 100)
    private String exeName;

    @NotNull
    private Integer editPassword;

    @NotNull
    private Integer pageAdd;

    @NotNull
    private Integer pageEdit;

    @NotNull
    private Integer pageDelete;

    @NotNull
    private Integer pageView;

    @NotNull
    private Integer pageActive;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotNull
    @Size(max = 50)
    private String modifiedBy;

    @NotNull
    private Integer show;

    @NotNull
    private Integer active;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer pView;

    @Size(max = 20)
    private String icon;

    private Integer mobileApp;
}
