package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.Classification;
import my.maleva.api.dto.ClassificationDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClassificationMapper {

    ClassificationDto toDto(Classification entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Classification toEntity(ClassificationDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ClassificationDto dto, @MappingTarget Classification entity);
}
