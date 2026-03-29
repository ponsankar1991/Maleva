package my.maleva.api.module.user.mapper;

import org.mapstruct.*;
import my.maleva.api.module.user.entity.MENUMaster;
import my.maleva.api.module.user.dto.MENUMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MENUMasterMapper {

    MENUMasterDto toDto(MENUMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MENUMaster toEntity(MENUMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(MENUMasterDto dto, @MappingTarget MENUMaster entity);
}
