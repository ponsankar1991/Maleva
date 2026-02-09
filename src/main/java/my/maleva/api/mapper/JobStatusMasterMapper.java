package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.JobStatusMaster;
import my.maleva.api.dto.JobStatusMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobStatusMasterMapper {

    JobStatusMasterDto toDto(JobStatusMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    JobStatusMaster toEntity(JobStatusMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(JobStatusMasterDto dto, @MappingTarget JobStatusMaster entity);
}
