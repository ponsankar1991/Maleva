package my.maleva.api.module.rti.mapper;

import my.maleva.api.module.rti.dto.RTIMasterDto;
import my.maleva.api.module.rti.entity.RTIMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * RTIMasterMapper
 * MapStruct mapper for RTIMaster entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RTIMasterMapper {

    /**
     * Convert RTIMaster entity to DTO
     */
    RTIMasterDto toDto(RTIMaster entity);

    /**
     * Convert RTIMaster DTO to entity
     */
    RTIMaster toEntity(RTIMasterDto dto);

    /**
     * Update RTIMaster entity from DTO
     */
    void updateEntityFromDto(RTIMasterDto dto, @MappingTarget RTIMaster entity);
}

