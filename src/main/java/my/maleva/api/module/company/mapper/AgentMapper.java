package my.maleva.api.module.company.mapper;

import org.mapstruct.*;
import my.maleva.api.module.company.entity.Agent;
import my.maleva.api.module.company.dto.AgentDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AgentMapper {

    @Mapping(source = "agentName", target = "Name")
    AgentDto toDto(Agent entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Agent toEntity(AgentDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AgentDto dto, @MappingTarget Agent entity);
}
