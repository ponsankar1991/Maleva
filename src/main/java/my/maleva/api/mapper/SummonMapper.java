package my.maleva.api.mapper;

import my.maleva.api.dto.SummonDto;
import my.maleva.api.model.Summon;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SummonMapper - MapStruct mapper for Summon
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SummonMapper {

    SummonDto toDto(Summon entity);

    Summon toEntity(SummonDto dto);

    void updateEntityFromDto(SummonDto dto, @MappingTarget Summon entity);
}

