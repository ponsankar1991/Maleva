package my.maleva.api.module.employee.mapper;

import org.mapstruct.Mapper;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import my.maleva.api.module.employee.dto.EmployeeAllDto;

/**
 * Mapper for EmployeeMaster to EmployeeAllDto conversion
 */
@Mapper(componentModel = "spring")
public interface EmployeeAllMapper {

    EmployeeAllDto toDto(EmployeeMaster entity);

    EmployeeMaster toEntity(EmployeeAllDto dto);
}

