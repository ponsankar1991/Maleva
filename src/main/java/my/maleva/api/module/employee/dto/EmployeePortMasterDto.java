package my.maleva.api.module.employee.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
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
public class EmployeePortMasterDto {

    private Integer id;

    @NotNull(message = "CompanyRefId is required")
    private Integer companyRefId;

    @NotNull(message = "EmployeeRefId is required")
    private Integer employeeRefId;

    @NotNull(message = "PortRefId is required")
    private Integer portRefId;

    private Integer assignedBy;

    private Integer active;

    private LocalDateTime createdDate;
    
    private LocalDateTime modifiedDate;
    
    private String modifiedBy;
}
