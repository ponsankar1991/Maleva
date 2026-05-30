package my.maleva.api.module.accountsgroupmaster.mapper;

import my.maleva.api.module.accountsgroupmaster.dto.ClassificationDto;
import my.maleva.api.module.accountsgroupmaster.entity.Classification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClassificationMapper {

    ClassificationDto toDto(Classification entity);

    Classification toEntity(ClassificationDto dto);
}

