package my.maleva.api.module.rti.mapper;

import my.maleva.api.module.rti.dto.RTIMasterDto;
import my.maleva.api.module.rti.entity.RTIMaster;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * RTIMasterMapper
 * MapStruct mapper for RTIMaster entity to DTO and vice versa.
 *
 * The RTI master REST endpoints load detail rows separately, so the lazy
 * rtiDetails collection is intentionally ignored here. That avoids
 * LazyInitializationException when open-in-view is disabled.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface RTIMasterMapper {

    /**
     * Convert RTIMaster entity to DTO.
     */
    @Mapping(target = "rtiDetails", ignore = true)
    RTIMasterDto toDto(RTIMaster entity);

    /**
     * Convert RTIMaster DTO to entity.
     */
    @Mapping(target = "rtiDetails", ignore = true)
    RTIMaster toEntity(RTIMasterDto dto);

    /**
     * Update RTIMaster entity from DTO.
     */
    @Mapping(target = "rtiDetails", ignore = true)
    void updateEntityFromDto(RTIMasterDto dto, @MappingTarget RTIMaster entity);
}
