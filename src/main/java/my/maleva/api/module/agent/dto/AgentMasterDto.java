package my.maleva.api.module.agent.dto;

import my.maleva.api.module.agent.entity.AgentRole;

import java.time.LocalDateTime;

public record AgentMasterDto(
        Integer id,
        Integer companyRefId,
        String agentName,
        String locationCode,
        String phoneNumber,
        AgentRole agentRole,
        Boolean active,
        LocalDateTime createdDate,
        String createdBy,
        LocalDateTime modifiedDate,
        String modifiedBy
) {}
