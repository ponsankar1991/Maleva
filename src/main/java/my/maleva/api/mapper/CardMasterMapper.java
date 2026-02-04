package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.CardMaster;
import my.maleva.api.dto.CardMasterDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CardMasterMapper {

    CardMasterDto toDto(CardMaster entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CardMaster toEntity(CardMasterDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CardMasterDto dto, @MappingTarget CardMaster entity);
}
