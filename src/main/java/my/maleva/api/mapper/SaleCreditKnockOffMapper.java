package my.maleva.api.mapper;

import my.maleva.api.dto.SaleCreditKnockOffDto;
import my.maleva.api.model.SaleCreditKnockOff;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleCreditKnockOffMapper
 * MapStruct mapper for SaleCreditKnockOff entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SaleCreditKnockOffMapper {

    /**
     * Convert SaleCreditKnockOff entity to DTO
     */
    SaleCreditKnockOffDto toDto(SaleCreditKnockOff entity);

    /**
     * Convert SaleCreditKnockOff DTO to entity
     */
    SaleCreditKnockOff toEntity(SaleCreditKnockOffDto dto);

    /**
     * Update SaleCreditKnockOff entity from DTO
     */
    void updateEntityFromDto(SaleCreditKnockOffDto dto, @MappingTarget SaleCreditKnockOff entity);
}

