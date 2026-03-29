package my.maleva.api.module.jobs.mapper;

import org.mapstruct.*;
import my.maleva.api.module.jobs.entity.ItemMasterJobDetails;
import my.maleva.api.module.jobs.dto.ItemMasterJobDetailsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ItemMasterJobDetailsMapper {

    ItemMasterJobDetailsDto toDto(ItemMasterJobDetails entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ItemMasterJobDetails toEntity(ItemMasterJobDetailsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(ItemMasterJobDetailsDto dto, @MappingTarget ItemMasterJobDetails entity);
}
