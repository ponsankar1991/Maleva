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
public class PortMasterDto {

    private Integer id;

    @NotNull(message = "Company reference ID is required")
    private Integer companyRefId;

    @NotBlank(message = "Port name is required")
    @Size(max = 50, message = "Port name cannot exceed 50 characters")
    private String portName;

    @NotNull(message = "Active status is required")
    private Integer active;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    @Size(max = 50, message = "Modified by cannot exceed 50 characters")
    private String modifiedBy;
}

