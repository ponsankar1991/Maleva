package my.maleva.api.module.employee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import my.maleva.api.module.employee.entity.EmployeePortMaster;
import my.maleva.api.module.employee.dto.EmployeePortMasterDto;

@Mapper(componentModel = "spring")
public interface EmployeePortMasterMapper {
    EmployeePortMasterDto toDto(EmployeePortMaster entity);
    EmployeePortMaster toEntity(EmployeePortMasterDto dto);
    void updateFromDto(EmployeePortMasterDto dto, @MappingTarget EmployeePortMaster entity);
}
