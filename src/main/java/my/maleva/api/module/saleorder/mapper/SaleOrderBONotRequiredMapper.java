package my.maleva.api.module.saleorder.mapper;

import my.maleva.api.module.saleorder.dto.SaleOrderBONotRequiredDto;
import my.maleva.api.module.saleorder.entity.SaleOrderBONotRequired;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleOrderBONotRequiredMapper - MapStruct mapper for SaleOrderBONotRequired
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderBONotRequiredMapper {

    SaleOrderBONotRequiredDto toDto(SaleOrderBONotRequired entity);

    SaleOrderBONotRequired toEntity(SaleOrderBONotRequiredDto dto);

    void updateEntityFromDto(SaleOrderBONotRequiredDto dto, @MappingTarget SaleOrderBONotRequired entity);
}

