package my.maleva.api.mapper;

import my.maleva.api.dto.SaleMasterReferenceDto;
import my.maleva.api.model.SaleMasterReference;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleMasterReferenceMapper - MapStruct mapper for SaleMasterReference
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleMasterReferenceMapper {

    SaleMasterReferenceDto toDto(SaleMasterReference entity);

    SaleMasterReference toEntity(SaleMasterReferenceDto dto);

    void updateEntityFromDto(SaleMasterReferenceDto dto, @MappingTarget SaleMasterReference entity);
}

