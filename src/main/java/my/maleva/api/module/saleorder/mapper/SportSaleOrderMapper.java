package my.maleva.api.module.saleorder.mapper;

import my.maleva.api.module.saleorder.dto.SportSaleOrderDto;
import my.maleva.api.module.saleorder.entity.SportSaleOrder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SportSaleOrderMapper - MapStruct mapper for SportSaleOrder
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SportSaleOrderMapper {

    SportSaleOrderDto toDto(SportSaleOrder entity);

    SportSaleOrder toEntity(SportSaleOrderDto dto);

    void updateEntityFromDto(SportSaleOrderDto dto, @MappingTarget SportSaleOrder entity);
}

