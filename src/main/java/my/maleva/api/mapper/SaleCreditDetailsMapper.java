package my.maleva.api.mapper;

import my.maleva.api.dto.SaleCreditDetailsDto;
import my.maleva.api.model.SaleCreditDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleCreditDetailsMapper
 * MapStruct mapper for SaleCreditDetails entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SaleCreditDetailsMapper {

    /**
     * Convert SaleCreditDetails entity to DTO
     */
    SaleCreditDetailsDto toDto(SaleCreditDetails entity);

    /**
     * Convert SaleCreditDetails DTO to entity
     */
    SaleCreditDetails toEntity(SaleCreditDetailsDto dto);

    /**
     * Update SaleCreditDetails entity from DTO
     */
    void updateEntityFromDto(SaleCreditDetailsDto dto, @MappingTarget SaleCreditDetails entity);
}

