package my.maleva.api.mapper;

import my.maleva.api.dto.SaleCreditMasterDto;
import my.maleva.api.model.SaleCreditMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleCreditMasterMapper
 * MapStruct mapper for SaleCreditMaster entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SaleCreditMasterMapper {

    /**
     * Convert SaleCreditMaster entity to DTO
     */
    SaleCreditMasterDto toDto(SaleCreditMaster entity);

    /**
     * Convert SaleCreditMaster DTO to entity
     */
    SaleCreditMaster toEntity(SaleCreditMasterDto dto);

    /**
     * Update SaleCreditMaster entity from DTO
     */
    void updateEntityFromDto(SaleCreditMasterDto dto, @MappingTarget SaleCreditMaster entity);
}

