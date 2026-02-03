package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.Uom;
import my.maleva.api.dto.UomDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UomMapper {

    UomDto toDto(Uom entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Uom toEntity(UomDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UomDto dto, @MappingTarget Uom entity);
}
