package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.ImageUpload;
import my.maleva.api.dto.ImageUploadDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ImageUploadMapper {

    ImageUploadDto toDto(ImageUpload entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ImageUpload toEntity(ImageUploadDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ImageUploadDto dto, @MappingTarget ImageUpload entity);
}
