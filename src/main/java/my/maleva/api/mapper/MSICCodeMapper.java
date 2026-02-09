package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.MSICCode;
import my.maleva.api.dto.MSICCodeDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MSICCodeMapper {

    MSICCodeDto toDto(MSICCode entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MSICCode toEntity(MSICCodeDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(MSICCodeDto dto, @MappingTarget MSICCode entity);
}
