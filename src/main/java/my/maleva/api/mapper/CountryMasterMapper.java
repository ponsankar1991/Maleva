package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.CountryMaster;
import my.maleva.api.dto.CountryMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CountryMasterMapper {

    CountryMasterDto toDto(CountryMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CountryMaster toEntity(CountryMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CountryMasterDto dto, @MappingTarget CountryMaster entity);
}
