package my.maleva.api.mapper;

import my.maleva.api.dto.SaleOrderMasterDto;
import my.maleva.api.model.SaleOrderMaster;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleOrderMasterMapper - MapStruct mapper for SaleOrderMaster
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderMasterMapper {
    SaleOrderMasterDto toDto(SaleOrderMaster entity);
    SaleOrderMaster toEntity(SaleOrderMasterDto dto);
    void updateEntityFromDto(SaleOrderMasterDto dto, @MappingTarget SaleOrderMaster entity);
}

