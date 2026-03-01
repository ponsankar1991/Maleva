package my.maleva.api.mapper;

import my.maleva.api.dto.VesselPlanningDetailsDto;
import my.maleva.api.model.VesselPlanningDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * VesselPlanningDetailsMapper - MapStruct mapper for VesselPlanningDetails
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VesselPlanningDetailsMapper {

    VesselPlanningDetailsDto toDto(VesselPlanningDetails entity);

    VesselPlanningDetails toEntity(VesselPlanningDetailsDto dto);

    void updateEntityFromDto(VesselPlanningDetailsDto dto, @MappingTarget VesselPlanningDetails entity);
}

