package my.maleva.api.module.saleorder.mapper;

import my.maleva.api.module.saleorder.dto.SaleOrderPickupDto;
import my.maleva.api.module.saleorder.entity.SaleOrderPickup;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * SaleOrderPickupMapper - MapStruct mapper for SaleOrderPickup
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SaleOrderPickupMapper {

    SaleOrderPickupDto toDto(SaleOrderPickup entity);

    SaleOrderPickup toEntity(SaleOrderPickupDto dto);

    void updateEntityFromDto(SaleOrderPickupDto dto, @MappingTarget SaleOrderPickup entity);
}

