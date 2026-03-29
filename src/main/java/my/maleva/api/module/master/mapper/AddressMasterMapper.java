package my.maleva.api.module.master.mapper;

import org.mapstruct.*;
import my.maleva.api.module.master.entity.AddressMaster;
import my.maleva.api.module.master.dto.AddressMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMasterMapper {

    AddressMasterDto toDto(AddressMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AddressMaster toEntity(AddressMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AddressMasterDto dto, @MappingTarget AddressMaster entity);
}
