package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.JobTypeMaster;
import my.maleva.api.dto.JobTypeMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobTypeMasterMapper {

    JobTypeMasterDto toDto(JobTypeMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    JobTypeMaster toEntity(JobTypeMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(JobTypeMasterDto dto, @MappingTarget JobTypeMaster entity);
}
