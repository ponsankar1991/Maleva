package my.maleva.api.module.fleet.mapper;

import org.mapstruct.*;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import my.maleva.api.module.employee.dto.EmployeeMasterDto;
import my.maleva.api.common.constant.UserRoles;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMasterMapper {

    @Mapping(target = "role", expression = "java(idToRole(entity.getRoleId()))")
    EmployeeMasterDto toDto(EmployeeMaster entity);

    EmployeeMaster toEntity(EmployeeMasterDto dto);

    // Prevent MapStruct from copying the identifier from DTO to the managed entity
    // Also protect critical internal system fields (cNumber, accountRefid) from being 
    // accidentally wiped out if the frontend sends 0 or empty strings during updates.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "CNumber", ignore = true)
    @Mapping(target = "CNumberDisplay", ignore = true)
    @Mapping(target = "accountRefid", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @InheritConfiguration(name = "toEntity")
    void updateFromDto(EmployeeMasterDto dto, @MappingTarget EmployeeMaster entity);

    @Named("idToRole")
    default UserRoles idToRole(Integer id) {
        if (id == null) return null;
        return UserRoles.fromId(id).orElse(null);
    }

    @Named("roleToId")
    default Integer roleToId(UserRoles role) {
        return role == null ? null : role.getRoleId();
    }
}
