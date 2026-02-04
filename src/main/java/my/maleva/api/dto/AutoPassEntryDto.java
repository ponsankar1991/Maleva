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
public class AutoPassEntryDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    private Integer userRefId;
    private Integer employeeRefId;
    private Integer lastEmployeeRefId;
    private Integer truckRefid;
    private Integer driverRefId;
    private Integer rtiRefId;

    @NotNull
    private LocalDateTime saleDate;

    @NotBlank
    @Size(max = 300)
    private String cNumberDisplay;

    @NotNull
    private Integer cNumber;

    @Size(max = 2000)
    private String remarks;

    @NotNull
    private Integer active;

    @NotNull
    private Float amount;

    @Size(max = 3000)
    private String filePath;

    private LocalDateTime createdDate;

    @NotBlank
    @Size(max = 50)
    private String createdBy;

    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @Size(max = 50)
    private String enterLink;

    @Size(max = 50)
    private String exitLink;
}
