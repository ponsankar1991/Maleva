package my.maleva.api.module.fleet.mapper;

import my.maleva.api.module.fleet.dto.SubcdiyEntryDto;
import my.maleva.api.module.fleet.entity.SubcdiyEntry;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SubcdiyEntryMapper - MapStruct mapper for SubcdiyEntry
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubcdiyEntryMapper {

    SubcdiyEntryDto toDto(SubcdiyEntry entity);

    SubcdiyEntry toEntity(SubcdiyEntryDto dto);

    void updateEntityFromDto(SubcdiyEntryDto dto, @MappingTarget SubcdiyEntry entity);
}

