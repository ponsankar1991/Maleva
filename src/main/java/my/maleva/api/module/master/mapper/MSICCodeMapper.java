package my.maleva.api.module.master.mapper;

import org.mapstruct.*;
import my.maleva.api.module.master.entity.MSICCode;
import my.maleva.api.module.master.dto.MSICCodeDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MSICCodeMapper {

    MSICCodeDto toDto(MSICCode entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MSICCode toEntity(MSICCodeDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(MSICCodeDto dto, @MappingTarget MSICCode entity);
}
