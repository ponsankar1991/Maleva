package my.maleva.api.module.master.mapper;

import org.mapstruct.*;
import my.maleva.api.module.master.entity.CountryMaster;
import my.maleva.api.module.master.dto.CountryMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CountryMasterMapper {

    CountryMasterDto toDto(CountryMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CountryMaster toEntity(CountryMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CountryMasterDto dto, @MappingTarget CountryMaster entity);
}
