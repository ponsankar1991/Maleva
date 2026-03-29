package my.maleva.api.module.fleet.mapper;

import my.maleva.api.module.fleet.dto.TollEntryDto;
import my.maleva.api.module.fleet.entity.TollEntry;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * TollEntryMapper - MapStruct mapper for TollEntry
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TollEntryMapper {

    TollEntryDto toDto(TollEntry entity);

    TollEntry toEntity(TollEntryDto dto);

    void updateEntityFromDto(TollEntryDto dto, @MappingTarget TollEntry entity);
}

