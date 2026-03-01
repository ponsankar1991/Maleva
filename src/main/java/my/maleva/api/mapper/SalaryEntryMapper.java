package my.maleva.api.mapper;

import my.maleva.api.dto.SalaryEntryDto;
import my.maleva.api.model.SalaryEntry;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SalaryEntryMapper
 * MapStruct mapper for SalaryEntry entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SalaryEntryMapper {

    /**
     * Convert SalaryEntry entity to DTO
     */
    SalaryEntryDto toDto(SalaryEntry entity);

    /**
     * Convert SalaryEntry DTO to entity
     */
    SalaryEntry toEntity(SalaryEntryDto dto);

    /**
     * Update SalaryEntry entity from DTO
     */
    void updateEntityFromDto(SalaryEntryDto dto, @MappingTarget SalaryEntry entity);
}

