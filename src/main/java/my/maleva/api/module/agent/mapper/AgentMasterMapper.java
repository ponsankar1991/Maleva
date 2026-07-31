package my.maleva.api.module.agent.mapper;

import my.maleva.api.module.agent.dto.AgentMasterCreateRequest;
import my.maleva.api.module.agent.dto.AgentMasterDto;
import my.maleva.api.module.agent.dto.AgentMasterUpdateRequest;
import my.maleva.api.module.agent.entity.AgentMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AgentMasterMapper {

    AgentMasterDto toDto(AgentMaster entity);

    AgentMaster toEntity(AgentMasterCreateRequest request);

    void updateEntity(@MappingTarget AgentMaster entity, AgentMasterUpdateRequest request);
}
