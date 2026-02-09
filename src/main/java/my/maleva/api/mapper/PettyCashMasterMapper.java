package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.PettyCashMaster;
import my.maleva.api.dto.PettyCashMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PettyCashMasterMapper {

    PettyCashMasterDto toDto(PettyCashMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PettyCashMaster toEntity(PettyCashMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PettyCashMasterDto dto, @MappingTarget PettyCashMaster entity);
}
