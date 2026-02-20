package my.maleva.api.agentcompany.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating an agent company.
 * Excludes auto-managed fields like CreatedDate and ModifiedDate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentCompanyRequestDTO {

    private Integer companyRefId;
    private String name;
    private Integer dFlag;
    private Integer active;
}

