package my.maleva.api.mapper;

import my.maleva.api.dto.PlanningMasterDto;
import my.maleva.api.model.PlanningMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PlanningMasterMapper {

    /**
     * Map PlanningMaster entity to PlanningMasterDto
     */
    PlanningMasterDto toDto(PlanningMaster entity);

    /**
     * Map PlanningMasterDto to PlanningMaster entity
     */
    PlanningMaster toEntity(PlanningMasterDto dto);

    /**
     * Update PlanningMaster entity from PlanningMasterDto
     */
    void updateFromDto(PlanningMasterDto dto, @MappingTarget PlanningMaster entity);
}

