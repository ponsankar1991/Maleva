package my.maleva.api.module.invoice.mapper;

import my.maleva.api.module.invoice.dto.SaleMasterReferenceDto;
import my.maleva.api.module.invoice.entity.SaleMasterReference;
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

