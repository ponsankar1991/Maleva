package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.DoMaster;
import my.maleva.api.dto.DoMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DoMasterMapper {

    DoMasterDto toDto(DoMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DoMaster toEntity(DoMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(DoMasterDto dto, @MappingTarget DoMaster entity);
}
