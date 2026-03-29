package my.maleva.api.module.saleorder.mapper;

import my.maleva.api.module.saleorder.dto.SaleOrderBODto;
import my.maleva.api.module.saleorder.entity.SaleOrderBO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleOrderBOMapper - MapStruct mapper for SaleOrderBO
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderBOMapper {

    SaleOrderBODto toDto(SaleOrderBO entity);

    SaleOrderBO toEntity(SaleOrderBODto dto);

    void updateEntityFromDto(SaleOrderBODto dto, @MappingTarget SaleOrderBO entity);
}

