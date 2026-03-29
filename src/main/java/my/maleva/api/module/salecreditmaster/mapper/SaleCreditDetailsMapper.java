package my.maleva.api.module.salecreditmaster.mapper;

import my.maleva.api.module.salecreditmaster.dto.SaleCreditDetailsDto;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditDetails;
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

