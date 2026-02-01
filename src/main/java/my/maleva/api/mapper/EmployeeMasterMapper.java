package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.EmployeeMaster;
import my.maleva.api.dto.EmployeeMasterDto;
import my.maleva.api.util.UserRoles;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMasterMapper {

    @Mapping(target = "role", expression = "java(idToRole(entity.getRoleId()))")
    EmployeeMasterDto toDto(EmployeeMaster entity);

    @Mappings({
            @Mapping(target = "roleId", expression = "java(roleToId(dto.getRole()))")
    })
    EmployeeMaster toEntity(EmployeeMasterDto dto);

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
