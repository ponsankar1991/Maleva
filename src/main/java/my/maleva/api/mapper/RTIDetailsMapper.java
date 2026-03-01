package my.maleva.api.mapper;

import my.maleva.api.dto.RTIDetailsDto;
import my.maleva.api.model.RTIDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * RTIDetailsMapper
 * MapStruct mapper for RTIDetails entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RTIDetailsMapper {

    /**
     * Convert RTIDetails entity to DTO
     */
    RTIDetailsDto toDto(RTIDetails entity);

    /**
     * Convert RTIDetails DTO to entity
     */
    RTIDetails toEntity(RTIDetailsDto dto);

    /**
     * Update RTIDetails entity from DTO
     */
    void updateEntityFromDto(RTIDetailsDto dto, @MappingTarget RTIDetails entity);
}

