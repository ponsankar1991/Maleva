package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.LocationMaster;
import my.maleva.api.dto.LocationMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationMasterMapper {

    LocationMasterDto toDto(LocationMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LocationMaster toEntity(LocationMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(LocationMasterDto dto, @MappingTarget LocationMaster entity);
}
