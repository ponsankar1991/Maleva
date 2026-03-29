package my.maleva.api.module.fleet.mapper;

import my.maleva.api.module.fleet.dto.TruckMasterDto;
import my.maleva.api.module.fleet.entity.TruckMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * TruckMasterMapper - MapStruct mapper for TruckMaster
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TruckMasterMapper {

    TruckMasterDto toDto(TruckMaster entity);

    TruckMaster toEntity(TruckMasterDto dto);

    void updateEntityFromDto(TruckMasterDto dto, @MappingTarget TruckMaster entity);
}

