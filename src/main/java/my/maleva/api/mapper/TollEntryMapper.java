package my.maleva.api.mapper;

import my.maleva.api.dto.TollEntryDto;
import my.maleva.api.model.TollEntry;
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

