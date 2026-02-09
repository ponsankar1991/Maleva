package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.PettyCashDetail;
import my.maleva.api.dto.PettyCashDetailDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PettyCashDetailMapper {

    PettyCashDetailDto toDto(PettyCashDetail entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PettyCashDetail toEntity(PettyCashDetailDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(PettyCashDetailDto dto, @MappingTarget PettyCashDetail entity);
}
