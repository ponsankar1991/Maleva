package my.maleva.api.module.saleorder.mapper;

import my.maleva.api.module.saleorder.dto.SaleOrderDeliveryDto;
import my.maleva.api.module.saleorder.entity.SaleOrderDelivery;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleOrderDeliveryMapper - MapStruct mapper for SaleOrderDelivery
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderDeliveryMapper {

    SaleOrderDeliveryDto toDto(SaleOrderDelivery entity);

    SaleOrderDelivery toEntity(SaleOrderDeliveryDto dto);

    void updateEntityFromDto(SaleOrderDeliveryDto dto, @MappingTarget SaleOrderDelivery entity);
}

