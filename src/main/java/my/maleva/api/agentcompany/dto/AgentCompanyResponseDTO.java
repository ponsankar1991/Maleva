package my.maleva.api.agentcompany.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for returning agent company information.
 * Includes all fields including auto-managed fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentCompanyResponseDTO {

    private Long id;
    private Integer companyRefId;
    private String name;
    private Integer dFlag;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    private Integer active;
}

