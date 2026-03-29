package my.maleva.api.module.agentcompany.mapper;

import my.maleva.api.module.agentcompany.entity.AgentCompanyMaster;
import my.maleva.api.module.agentcompany.dto.AgentCompanyMasterDTO;
import my.maleva.api.module.agentcompany.dto.AgentCompanyRequestDTO;
import my.maleva.api.module.agentcompany.dto.AgentCompanyResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for AgentCompanyMaster entity and DTOs.
 * Handles conversions between entity, DTO, request DTO, and response DTO.
 */
@Mapper(componentModel = "spring")
public interface AgentCompanyMasterMapper {

    // Standard DTO mappings
    AgentCompanyMasterDTO toDto(AgentCompanyMaster entity);

    AgentCompanyMaster toEntity(AgentCompanyMasterDTO dto);

    void updateEntityFromDto(AgentCompanyMasterDTO dto, @MappingTarget AgentCompanyMaster entity);

    // Response DTO mappings
    AgentCompanyResponseDTO toResponseDto(AgentCompanyMaster entity);

    AgentCompanyMaster toEntityFromResponse(AgentCompanyResponseDTO dto);

    // Request DTO mappings
    AgentCompanyRequestDTO toRequestDto(AgentCompanyMaster entity);

    AgentCompanyMaster toEntityFromRequest(AgentCompanyRequestDTO dto);

    // DTO to Request/Response conversions
    AgentCompanyResponseDTO dtoToResponseDto(AgentCompanyMasterDTO dto);

    AgentCompanyRequestDTO dtoToRequestDto(AgentCompanyMasterDTO dto);
}
