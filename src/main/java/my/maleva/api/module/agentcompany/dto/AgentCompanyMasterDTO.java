package my.maleva.api.module.agentcompany.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for AgentCompanyMaster.
 * Used for internal API communication and mapping between entity and request/response DTOs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentCompanyMasterDTO {

    private Long id;
    private Integer companyRefId;
    private String name;
    private Integer dFlag;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    private Integer active;
}
