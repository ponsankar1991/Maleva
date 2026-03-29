package my.maleva.api.module.master.mapper;

import my.maleva.api.module.master.dto.SymbolMasterDto;
import my.maleva.api.module.master.entity.SymbolMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SymbolMasterMapper - MapStruct mapper for SymbolMaster
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SymbolMasterMapper {

    SymbolMasterDto toDto(SymbolMaster entity);

    SymbolMaster toEntity(SymbolMasterDto dto);

    void updateEntityFromDto(SymbolMasterDto dto, @MappingTarget SymbolMaster entity);
}

