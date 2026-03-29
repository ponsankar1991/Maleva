package my.maleva.api.module.itemmaster.mapper;

import org.mapstruct.*;
import my.maleva.api.module.itemmaster.entity.ItemMasterCStock;
import my.maleva.api.module.itemmaster.dto.ItemMasterCStockDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ItemMasterCStockMapper {

    ItemMasterCStockDto toDto(ItemMasterCStock entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ItemMasterCStock toEntity(ItemMasterCStockDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ItemMasterCStockDto dto, @MappingTarget ItemMasterCStock entity);
}
