package my.maleva.api.module.master.mapper;

import my.maleva.api.module.master.dto.SymbolMasterDto;
import my.maleva.api.module.master.entity.SymbolMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * SymbolMasterMapper - MapStruct mapper for SymbolMaster
 * FINAL VERSION: Entity and DTO field names now match (SName, CName, DFlag)
 * No explicit @Mapping annotations needed - MapStruct auto-detects matching field names
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface SymbolMasterMapper {

    /**
     * Convert Entity to DTO
     * MapStruct automatically matches: Entity.SName → DTO.SName
     */
    SymbolMasterDto toDto(SymbolMaster entity);

    /**
     * Convert DTO to Entity
     * MapStruct automatically matches: DTO.SName → Entity.SName
     */
    SymbolMaster toEntity(SymbolMasterDto dto);

    /**
     * Update existing entity from DTO
     */
    void updateEntityFromDto(SymbolMasterDto dto, @MappingTarget SymbolMaster entity);
}