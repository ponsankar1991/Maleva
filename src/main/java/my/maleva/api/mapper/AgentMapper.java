package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.Agent;
import my.maleva.api.dto.AgentDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AgentMapper {

    AgentDto toDto(Agent entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Agent toEntity(AgentDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AgentDto dto, @MappingTarget Agent entity);
}
