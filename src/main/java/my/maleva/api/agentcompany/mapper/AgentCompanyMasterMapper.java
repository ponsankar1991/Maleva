package my.maleva.api.agentcompany.mapper;

import my.maleva.api.agentcompany.entity.AgentCompanyMaster;
import my.maleva.api.agentcompany.dto.AgentCompanyMasterDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AgentCompanyMasterMapper {

    AgentCompanyMasterDTO toDto(AgentCompanyMaster entity);

    AgentCompanyMaster toEntity(AgentCompanyMasterDTO dto);

    void updateEntityFromDto(AgentCompanyMasterDTO dto, @MappingTarget AgentCompanyMaster entity);
}
