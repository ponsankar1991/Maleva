package my.maleva.api.mapper;

import my.maleva.api.dto.TollEntryDetailsDto;
import my.maleva.api.model.TollEntryDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * TollEntryDetailsMapper - MapStruct mapper for TollEntryDetails
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TollEntryDetailsMapper {

    TollEntryDetailsDto toDto(TollEntryDetails entity);

    TollEntryDetails toEntity(TollEntryDetailsDto dto);

    void updateEntityFromDto(TollEntryDetailsDto dto, @MappingTarget TollEntryDetails entity);
}

