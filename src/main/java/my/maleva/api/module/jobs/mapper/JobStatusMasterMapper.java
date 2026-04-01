package my.maleva.api.module.jobs.mapper;

import org.mapstruct.*;
import my.maleva.api.module.jobs.entity.JobStatusMaster;
import my.maleva.api.module.jobs.dto.JobStatusMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobStatusMasterMapper {

    @Mapping(source = "parentStatus.name", target = "mName")
    JobStatusMasterDto toDto(JobStatusMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "parentStatus", ignore = true)
    JobStatusMaster toEntity(JobStatusMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "parentStatus", ignore = true)
    void updateFromDto(JobStatusMasterDto dto, @MappingTarget JobStatusMaster entity);
}
