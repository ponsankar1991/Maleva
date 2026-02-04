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
public class CashierDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotBlank
    @Size(max = 50)
    private String cashierName;

    @NotBlank
    @Size(max = 50)
    private String pswd;

    @NotNull
    private Boolean status;

    @NotNull
    private Integer active;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @NotNull
    private Boolean deleteRow;

    @NotNull
    private Boolean deleteReason;
}
