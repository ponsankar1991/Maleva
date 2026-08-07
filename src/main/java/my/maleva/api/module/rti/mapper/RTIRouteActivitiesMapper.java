package my.maleva.api.module.rti.mapper;

import my.maleva.api.module.rti.dto.RTIRouteActivitiesDto;
import my.maleva.api.module.rti.entity.RTIRouteActivities;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RTIRouteActivitiesMapper {
    RTIRouteActivitiesDto toDto(RTIRouteActivities entity);
    RTIRouteActivities toEntity(RTIRouteActivitiesDto dto);
    void updateEntityFromDto(RTIRouteActivitiesDto dto, @MappingTarget RTIRouteActivities entity);
}
