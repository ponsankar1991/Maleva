package my.maleva.api.mapper;

import my.maleva.api.dto.VesselPlanningMasterDto;
import my.maleva.api.model.VesselPlanningMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * VesselPlanningMasterMapper - MapStruct mapper for VesselPlanningMaster
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VesselPlanningMasterMapper {

    VesselPlanningMasterDto toDto(VesselPlanningMaster entity);

    VesselPlanningMaster toEntity(VesselPlanningMasterDto dto);

    void updateEntityFromDto(VesselPlanningMasterDto dto, @MappingTarget VesselPlanningMaster entity);
}

