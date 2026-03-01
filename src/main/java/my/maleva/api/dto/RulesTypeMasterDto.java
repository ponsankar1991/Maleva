package my.maleva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * RulesTypeMasterDto
 * Data Transfer Object for RulesTypeMaster API layer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RulesTypeMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotBlank(message = "Rule Type Name is required")
    @Size(max = 100, message = "Rule Type Name cannot exceed 100 characters")
    private String ruleTypeName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Rule Type Code is required")
    @Size(max = 50, message = "Rule Type Code cannot exceed 50 characters")
    private String ruleTypeCode;

    private Integer active;

    private LocalDateTime createdDate;

    private String createdBy;

    private LocalDateTime modifiedDate;

    private String modifiedBy;
}

