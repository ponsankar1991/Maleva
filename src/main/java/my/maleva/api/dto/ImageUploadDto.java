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
public class ImageUploadDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    private Integer userRefId;
    private Integer employeeRefId;

    @NotNull
    private Integer saleOrderMasterRefId;

    @NotBlank
    @Size(max = 50)
    private String type;

    @NotBlank
    @Size(max = 3000)
    private String filePath;

    @NotNull
    private Integer active;

    private LocalDateTime createdDate;

    @NotBlank
    @Size(max = 50)
    private String createdBy;

    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    private Integer lastEmployeeRefId;
}
