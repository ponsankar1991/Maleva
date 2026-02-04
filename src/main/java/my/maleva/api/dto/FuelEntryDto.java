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
public class FuelEntryDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    private Integer userRefId;
    private Integer employeeRefId;
    private Integer lastEmployeeRefId;
    private Integer truckRefid;
    private Integer driverRefId;

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

    private Integer fStatus;

    @NotNull
    private Float aliter;

    @NotNull
    private Float aAmount;

    @NotNull
    private Float pliter;

    @NotNull
    private Float pRate;

    @NotNull
    private Float pAmount;

    @NotNull
    private Float gliter;

    @NotNull
    private Float gAmount;

    @NotNull
    private Float dPliter;

    @NotNull
    private Float dpAmount;

    @NotNull
    private Float dGliter;

    @NotNull
    private Float dgAmount;

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
}
