package my.maleva.api.module.user.mapper;

import org.mapstruct.*;
import my.maleva.api.module.user.entity.MENUPrivilege;
import my.maleva.api.module.user.dto.MENUPrivilegeDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MENUPrivilegeMapper {

    MENUPrivilegeDto toDto(MENUPrivilege entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MENUPrivilege toEntity(MENUPrivilegeDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(MENUPrivilegeDto dto, @MappingTarget MENUPrivilege entity);

    // MapStruct helper methods to convert Integer <-> Boolean for fields like editPassword, mobileApp
    default Boolean intToBoolean(Integer value) {
        return value != null && value != 0;
    }

    default Integer booleanToInt(Boolean value) {
        return (value != null && value) ? 1 : 0;
    }
}
