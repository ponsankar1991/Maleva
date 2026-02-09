package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.MasterSetting;
import my.maleva.api.dto.MasterSettingDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MasterSettingMapper {

    MasterSettingDto toDto(MasterSetting entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MasterSetting toEntity(MasterSettingDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(MasterSettingDto dto, @MappingTarget MasterSetting entity);

    default Boolean intToBoolean(Integer value) {
        return value != null && value != 0;
    }

    default Integer booleanToInt(Boolean value) {
        return (value != null && value) ? 1 : 0;
    }
}
