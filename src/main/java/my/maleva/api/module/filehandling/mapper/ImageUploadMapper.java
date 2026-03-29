package my.maleva.api.module.filehandling.mapper;

import org.mapstruct.*;
import my.maleva.api.module.filehandling.entity.ImageUpload;
import my.maleva.api.module.filehandling.dto.ImageUploadDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ImageUploadMapper {

    ImageUploadDto toDto(ImageUpload entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ImageUpload toEntity(ImageUploadDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ImageUploadDto dto, @MappingTarget ImageUpload entity);
}
