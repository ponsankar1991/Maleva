package my.maleva.api.module.pettycash.mapper;

import org.mapstruct.*;
import my.maleva.api.module.pettycash.entity.PettyCashDetail;
import my.maleva.api.module.pettycash.dto.PettyCashDetailDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PettyCashDetailMapper {

    PettyCashDetailDto toDto(PettyCashDetail entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PettyCashDetail toEntity(PettyCashDetailDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PettyCashDetailDto dto, @MappingTarget PettyCashDetail entity);
}
