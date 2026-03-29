package my.maleva.api.module.fleet.mapper;

import org.mapstruct.*;
import my.maleva.api.module.fleet.entity.FuelFillings;
import my.maleva.api.module.fleet.dto.FuelFillingsDto;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FuelFillingsMapper {

    FuelFillingsDto toDto(FuelFillings entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    FuelFillings toEntity(FuelFillingsDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(FuelFillingsDto dto, @MappingTarget FuelFillings entity);
}
