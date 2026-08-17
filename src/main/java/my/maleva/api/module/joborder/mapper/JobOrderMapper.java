package my.maleva.api.module.joborder.mapper;

import my.maleva.api.module.joborder.dto.JobOrderRequestDto;
import my.maleva.api.module.joborder.dto.JobOrderResponseDto;
import my.maleva.api.module.joborder.entity.JobOrderMaster;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {JobOrderDetailMapper.class})
public interface JobOrderMapper {

    @Mapping(target = "employeeName", source = "employee.employeeName")
    @Mapping(target = "truckName", source = "truck.truckName")
    @Mapping(target = "truckNumber", source = "truck.truckNumber")
    @Mapping(target = "driverName", source = "driver.driverName")
    @Mapping(target = "jobTypeName", source = "jobType.jobTypeName")
    @Mapping(target = "statusName", source = "status.statusName")
    @Mapping(target = "priorityName", source = "priority.priorityName")
    // RefIds mapped via entity structure
    @Mapping(target = "employeeRefId", source = "employee.id")
    @Mapping(target = "truckMasterRefId", source = "truck.id")
    @Mapping(target = "driverMasterRefId", source = "driver.id")
    @Mapping(target = "jobTypeRefId", source = "jobType.id")
    @Mapping(target = "statusRefId", source = "status.id")
    @Mapping(target = "priorityRefId", source = "priority.id")
    @Mapping(target = "cNumber", source = "CNumber")
    @Mapping(target = "cNumberDisplay", source = "CNumberDisplay")
    JobOrderResponseDto toDto(JobOrderMaster entity);

    // Entity is usually hydrated via Service layer to prevent detached entity errors,
    // so we ignore relations in toEntity and map them in the service.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "CNumber", ignore = true)
    @Mapping(target = "CNumberDisplay", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "truck", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "jobType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "details", ignore = true)
    JobOrderMaster toEntity(JobOrderRequestDto request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "CNumber", ignore = true)
    @Mapping(target = "CNumberDisplay", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "truck", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "jobType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "details", ignore = true)
    void updateEntity(@MappingTarget JobOrderMaster entity, JobOrderRequestDto request);
}
