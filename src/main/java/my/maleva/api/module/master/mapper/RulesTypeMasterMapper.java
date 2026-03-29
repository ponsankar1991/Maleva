package my.maleva.api.module.master.mapper;

import my.maleva.api.module.master.dto.RulesTypeMasterDto;
import my.maleva.api.module.master.entity.RulesTypeMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * RulesTypeMasterMapper
 * MapStruct mapper for RulesTypeMaster entity to DTO and vice versa
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RulesTypeMasterMapper {

    /**
     * Convert RulesTypeMaster entity to DTO
     */
    RulesTypeMasterDto toDto(RulesTypeMaster entity);

    /**
     * Convert RulesTypeMaster DTO to entity
     */
    RulesTypeMaster toEntity(RulesTypeMasterDto dto);

    /**
     * Update RulesTypeMaster entity from DTO
     */
    void updateEntityFromDto(RulesTypeMasterDto dto, @MappingTarget RulesTypeMaster entity);
}

