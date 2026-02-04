package com.maleva.agentcompany.mapper;

import com.maleva.agentcompany.entity.AgentCompanyMaster;
import com.maleva.agentcompany.dto.AgentCompanyMasterDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AgentCompanyMasterMapper {

    AgentCompanyMasterDTO toDto(AgentCompanyMaster entity);

    AgentCompanyMaster toEntity(AgentCompanyMasterDTO dto);

    void updateEntityFromDto(AgentCompanyMasterDTO dto, @MappingTarget AgentCompanyMaster entity);
}
