package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.FuelEntry;
import my.maleva.api.dto.FuelEntryDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FuelEntryMapper {

    FuelEntryDto toDto(FuelEntry entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    FuelEntry toEntity(FuelEntryDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(FuelEntryDto dto, @MappingTarget FuelEntry entity);
}
