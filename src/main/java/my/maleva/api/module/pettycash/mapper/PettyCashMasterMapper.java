package my.maleva.api.module.pettycash.mapper;

import org.mapstruct.*;
import my.maleva.api.module.pettycash.entity.PettyCashMaster;
import my.maleva.api.module.pettycash.dto.PettyCashMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PettyCashMasterMapper {

    PettyCashMasterDto toDto(PettyCashMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PettyCashMaster toEntity(PettyCashMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PettyCashMasterDto dto, @MappingTarget PettyCashMaster entity);
}
