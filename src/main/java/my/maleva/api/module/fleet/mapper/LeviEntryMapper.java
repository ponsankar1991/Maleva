package my.maleva.api.module.fleet.mapper;

import org.mapstruct.*;
import my.maleva.api.module.fleet.entity.LeviEntry;
import my.maleva.api.module.fleet.dto.LeviEntryDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LeviEntryMapper {

    LeviEntryDto toDto(LeviEntry entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LeviEntry toEntity(LeviEntryDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(LeviEntryDto dto, @MappingTarget LeviEntry entity);
}
