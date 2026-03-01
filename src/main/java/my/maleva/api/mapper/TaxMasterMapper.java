package my.maleva.api.mapper;

import my.maleva.api.dto.TaxMasterDto;
import my.maleva.api.model.TaxMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * TaxMasterMapper - MapStruct mapper for TaxMaster
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaxMasterMapper {

    TaxMasterDto toDto(TaxMaster entity);

    TaxMaster toEntity(TaxMasterDto dto);

    void updateEntityFromDto(TaxMasterDto dto, @MappingTarget TaxMaster entity);
}

