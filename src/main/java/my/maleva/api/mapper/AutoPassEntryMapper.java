package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.AutoPassEntry;
import my.maleva.api.dto.AutoPassEntryDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AutoPassEntryMapper {

    AutoPassEntryDto toDto(AutoPassEntry entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    AutoPassEntry toEntity(AutoPassEntryDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AutoPassEntryDto dto, @MappingTarget AutoPassEntry entity);
}
