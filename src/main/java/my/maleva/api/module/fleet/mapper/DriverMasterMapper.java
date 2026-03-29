package my.maleva.api.module.fleet.mapper;

import org.mapstruct.*;
import my.maleva.api.module.fleet.entity.DriverMaster;
import my.maleva.api.module.fleet.dto.DriverMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DriverMasterMapper {

    DriverMasterDto toDto(DriverMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DriverMaster toEntity(DriverMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(DriverMasterDto dto, @MappingTarget DriverMaster entity);
}
