package my.maleva.api.module.fleet.mapper;

import my.maleva.api.module.fleet.dto.SpeedReportDto;
import my.maleva.api.module.fleet.entity.SpeedReport;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SpeedReportMapper - MapStruct mapper for SpeedReport
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SpeedReportMapper {

    SpeedReportDto toDto(SpeedReport entity);

    SpeedReport toEntity(SpeedReportDto dto);

    void updateEntityFromDto(SpeedReportDto dto, @MappingTarget SpeedReport entity);
}

