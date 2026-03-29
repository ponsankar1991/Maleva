package my.maleva.api.module.invoice.mapper;

import my.maleva.api.module.invoice.dto.SaleMasterDto;
import my.maleva.api.module.invoice.entity.SaleMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleMasterMapper
 * MapStruct mapper for SaleMaster entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SaleMasterMapper {

    SaleMasterDto toDto(SaleMaster entity);
    SaleMaster toEntity(SaleMasterDto dto);
    void updateEntityFromDto(SaleMasterDto dto, @MappingTarget SaleMaster entity);
}

