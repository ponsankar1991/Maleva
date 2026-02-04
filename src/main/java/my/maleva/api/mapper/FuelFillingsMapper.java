package my.maleva.api.mapper;

import org.mapstruct.*;
import my.maleva.api.model.FuelFillings;
import my.maleva.api.dto.FuelFillingsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FuelFillingsMapper {

    FuelFillingsDto toDto(FuelFillings entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    FuelFillings toEntity(FuelFillingsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(FuelFillingsDto dto, @MappingTarget FuelFillings entity);
}
