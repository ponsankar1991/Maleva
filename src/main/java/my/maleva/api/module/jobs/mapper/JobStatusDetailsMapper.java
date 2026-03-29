package my.maleva.api.module.jobs.mapper;

import org.mapstruct.*;
import my.maleva.api.module.jobs.entity.JobStatusDetails;
import my.maleva.api.module.jobs.dto.JobStatusDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobStatusDetailsMapper {

    JobStatusDetailsDto toDto(JobStatusDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    JobStatusDetails toEntity(JobStatusDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(JobStatusDetailsDto dto, @MappingTarget JobStatusDetails entity);
}
