package my.maleva.api.module.fleet.mapper;

import my.maleva.api.module.fleet.dto.TruckSparePartsDto;
import my.maleva.api.module.fleet.entity.TruckSpareParts;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * TruckSparePartsMapper - MapStruct mapper for TruckSpareParts
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TruckSparePartsMapper {

    TruckSparePartsDto toDto(TruckSpareParts entity);

    TruckSpareParts toEntity(TruckSparePartsDto dto);

    void updateEntityFromDto(TruckSparePartsDto dto, @MappingTarget TruckSpareParts entity);
}

