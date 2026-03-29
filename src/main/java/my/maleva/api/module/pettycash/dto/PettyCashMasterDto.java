package my.maleva.api.module.pettycash.dto;

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
public class PettyCashMasterDto {
    private Integer id;

    @Size(max = 50)
    private String cNumberDisplay;

    @NotNull
    private Integer employeeRefId;

    @Size(max = 20)
    private String paymentStatus;

    private LocalDateTime pettyCashDate;

    @Size(max = 255)
    private String remark;

    private Integer status;

    private Integer active;

    @Size(max = 100)
    private String amount;

    private LocalDateTime createdDate;

    @Size(max = 50)
    private String createdBy;

    private LocalDateTime modifiedDate;

    @Size(max = 50)
    private String modifiedBy;

    private Integer companyRefId;

    private Integer cNumber;

    @Size(max = 255)
    private String department;
}
