package my.maleva.api.mapper;

import my.maleva.api.dto.SymbolMasterDto;
import my.maleva.api.model.SymbolMaster;
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

