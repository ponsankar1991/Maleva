package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.ItemMaster;
import my.maleva.api.dto.ItemMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ItemMasterMapper {

    ItemMasterDto toDto(ItemMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ItemMaster toEntity(ItemMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ItemMasterDto dto, @MappingTarget ItemMaster entity);
}
