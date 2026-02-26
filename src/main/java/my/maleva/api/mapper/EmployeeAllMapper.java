package my.maleva.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import my.maleva.api.model.EmployeeMaster;
import my.maleva.api.dto.EmployeeAllDto;

/**
 * Mapper for EmployeeMaster to EmployeeAllDto conversion
 */
@Mapper(componentModel = "spring")
public interface EmployeeAllMapper {

    EmployeeAllDto toDto(EmployeeMaster entity);

    EmployeeMaster toEntity(EmployeeAllDto dto);
}

