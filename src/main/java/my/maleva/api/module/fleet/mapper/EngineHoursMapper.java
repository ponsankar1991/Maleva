package my.maleva.api.module.fleet.mapper;

import org.mapstruct.*;
import my.maleva.api.module.fleet.entity.EngineHours;
import my.maleva.api.module.fleet.dto.EngineHoursDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EngineHoursMapper {

    EngineHoursDto toDto(EngineHours entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    EngineHours toEntity(EngineHoursDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(EngineHoursDto dto, @MappingTarget EngineHours entity);
}
