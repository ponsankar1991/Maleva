package my.maleva.api.module.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import my.maleva.api.module.agent.entity.AgentRole;

public record AgentMasterUpdateRequest(
        @NotNull(message = "Id is required for update")
        Integer id,

        @NotNull(message = "CompanyRefId is required")
        Integer companyRefId,

        @NotBlank(message = "AgentName is required")
        @Size(max = 100, message = "AgentName must not exceed 100 characters")
        String agentName,

        @NotBlank(message = "LocationCode is required")
        @Size(max = 30, message = "LocationCode must not exceed 30 characters")
        String locationCode,

        @Size(max = 20, message = "PhoneNumber must not exceed 20 characters")
        String phoneNumber,

        @NotNull(message = "AgentRole is required")
        AgentRole agentRole,

        @NotNull(message = "Active is required")
        Boolean active
) {}
