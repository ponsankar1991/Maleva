package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.LicenseMaster;
import my.maleva.api.dto.LicenseMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LicenseMasterMapper {

    LicenseMasterDto toDto(LicenseMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LicenseMaster toEntity(LicenseMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(LicenseMasterDto dto, @MappingTarget LicenseMaster entity);
}
