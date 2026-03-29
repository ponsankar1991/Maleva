package my.maleva.api.module.umo.mapper;

import org.mapstruct.*;
import my.maleva.api.module.umo.entity.Uom;
import my.maleva.api.module.umo.dto.UomDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UomMapper {

    UomDto toDto(Uom entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Uom toEntity(UomDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UomDto dto, @MappingTarget Uom entity);
}
