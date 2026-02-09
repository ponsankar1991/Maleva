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
public class LicenseMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private LocalDateTime lDate;

    @NotNull
    @Size(max = 1000)
    private String licenseName;

    @NotNull
    private LocalDateTime expiryDate;

    private Integer employeeRefId;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotNull
    @Size(max = 50)
    private String modifiedBy;

    @NotNull
    private Integer active;

    @Size(max = 250)
    private String category;
}
