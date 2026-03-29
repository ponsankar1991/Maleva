package my.maleva.api.module.company.mapper;

import org.mapstruct.*;
import my.maleva.api.module.company.entity.MainSetting;
import my.maleva.api.module.company.dto.MainSettingDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MainSettingMapper {

    MainSettingDto toDto(MainSetting entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MainSetting toEntity(MainSettingDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(MainSettingDto dto, @MappingTarget MainSetting entity);

    // MapStruct helper methods to convert Integer <-> Boolean used by many fields
    default Boolean intToBoolean(Integer value) {
        return value != null && value != 0;
    }

    default Integer booleanToInt(Boolean value) {
        return (value != null && value) ? 1 : 0;
    }
}
