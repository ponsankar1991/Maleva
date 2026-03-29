package my.maleva.api.module.fleet.mapper;

import my.maleva.api.module.fleet.dto.TollEntryDetailsDto;
import my.maleva.api.module.fleet.entity.TollEntryDetails;
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

