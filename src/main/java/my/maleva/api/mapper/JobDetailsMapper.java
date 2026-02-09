package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.JobDetails;
import my.maleva.api.dto.JobDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobDetailsMapper {

    JobDetailsDto toDto(JobDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    JobDetails toEntity(JobDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(JobDetailsDto dto, @MappingTarget JobDetails entity);
}
