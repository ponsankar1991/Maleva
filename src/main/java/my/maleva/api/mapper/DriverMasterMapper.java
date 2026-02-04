package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.DriverMaster;
import my.maleva.api.dto.DriverMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DriverMasterMapper {

    DriverMasterDto toDto(DriverMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DriverMaster toEntity(DriverMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(DriverMasterDto dto, @MappingTarget DriverMaster entity);
}
