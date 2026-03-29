package my.maleva.api.module.planning.mapper;

import my.maleva.api.module.planning.dto.PlanningDetailsDto;
import my.maleva.api.module.planning.entity.PlanningDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PlanningDetailsMapper {

    /**
     * Map PlanningDetails entity to PlanningDetailsDto
     */
    PlanningDetailsDto toDto(PlanningDetails entity);

    /**
     * Map PlanningDetailsDto to PlanningDetails entity
     */
    PlanningDetails toEntity(PlanningDetailsDto dto);

    /**
     * Update PlanningDetails entity from PlanningDetailsDto
     */
    void updateFromDto(PlanningDetailsDto dto, @MappingTarget PlanningDetails entity);
}

